package main

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"runtime"
	"time"

	"github.com/joho/godotenv"
	_ "github.com/lib/pq"
)

type City struct {
	Name string
	Lat  float64
	Lon  float64
}

type WeatherData struct {
	ID          int64     `json:"id"`
	Timestamp   time.Time `json:"timestamp"`
	Temperatura float64   `json:"temperatura"`
	Humedad     int       `json:"humedad"`
	EstadoClima string    `json:"estado_clima"`
}

var db *sql.DB
var loc *time.Location

var cities = []City{
	{Name: "BuenosAires", Lat: -34.6037, Lon: -58.3816},
}

func main() {
	// Cargar .env en local
	if err := godotenv.Load(); err != nil {
		log.Println("Aviso: no se encontró .env, usando entorno")
	}

	// Timezone Buenos Aires
	var err error
	loc, err = time.LoadLocation("America/Argentina/Buenos_Aires")
	if err != nil {
		log.Fatal(err)
	}

	// DATABASE_URL unificada (local + Railway)
	dbURL := fmt.Sprintf(
		"postgresql://%s:%s@%s:%s/%s?sslmode=disable",
		os.Getenv("POSTGRES_USER"),
		os.Getenv("POSTGRES_PASS"),
		os.Getenv("POSTGRES_HOST"),
		os.Getenv("POSTGRES_PORT"),
		os.Getenv("POSTGRES_DB"),
	)

	db, err = sql.Open("postgres", dbURL)
	if err != nil {
		log.Fatal("Error abriendo DB:", err)
	}
	defer db.Close()

	if err := db.Ping(); err != nil {
		log.Fatal("No se pudo conectar a la DB:", err)
	}

	// Worker horario
	go hourlyWorker()

	// Router
	mux := http.NewServeMux()
	mux.HandleFunc("/clima/latest", getLatestWeatherHandler)

	// CORS
	handler := cors(mux)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	printBanner(port)

	log.Println("Servidor iniciado con éxito")
	log.Fatal(http.ListenAndServe(":"+port, handler))
}

/* =========================
   CORS
========================= */

func cors(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type")

		if r.Method == http.MethodOptions {
			w.WriteHeader(http.StatusOK)
			return
		}

		next.ServeHTTP(w, r)
	})
}

/* =========================
   WORKER HORARIO
========================= */

func hourlyWorker() {
	for {
		now := time.Now().In(loc)
		next := now.Truncate(time.Hour).Add(time.Hour)
		time.Sleep(time.Until(next))

		for _, city := range cities {
			updateWeather(city)
		}
	}
}

/* =========================
   WEATHER
========================= */

func mapCondition(apiCondition string) string {
	switch apiCondition {
	case "Clear":
		return "SOLEADO"
	case "Clouds":
		return "NUBLADO"
	case "Rain", "Drizzle":
		return "LLUVIA"
	case "Thunderstorm":
		return "TORMENTA"
	case "Snow":
		return "NIEVE"
	case "Mist", "Fog", "Haze":
		return "NIEBLA"
	default:
		return "DESPEJADO"
	}
}

func updateWeather(city City) {
	apiKey := os.Getenv("OPENWEATHER_KEY")
	if apiKey == "" {
		log.Println("OPENWEATHER_KEY no configurada")
		return
	}

	url := fmt.Sprintf(
		"https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
		city.Lat, city.Lon, apiKey,
	)

	resp, err := http.Get(url)
	if err != nil {
		log.Printf("Error API (%s): %v", city.Name, err)
		return
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		log.Printf("Error API (%s): status %d", city.Name, resp.StatusCode)
		return
	}

	var result struct {
		Main struct {
			Temp     float64 `json:"temp"`
			Humidity int     `json:"humidity"`
		} `json:"main"`
		Weather []struct {
			Main string `json:"main"`
		} `json:"weather"`
	}

	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		log.Println("Error decode JSON:", err)
		return
	}

	if len(result.Weather) == 0 {
		log.Printf("Weather vacío (%s)", city.Name)
		return
	}

	estado := mapCondition(result.Weather[0].Main)

	// Hora local en punto (sin segundos ni millis)
	now := time.Now().In(loc)
	now = time.Date(
		now.Year(), now.Month(), now.Day(),
		now.Hour(), 0, 0, 0,
		loc,
	)

	_, err = db.Exec(
		`INSERT INTO clima (timestamp, temperatura, humedad, estado_clima)
		 VALUES ($1, $2, $3, $4)`,
		now,
		result.Main.Temp,
		result.Main.Humidity,
		estado,
	)

	if err != nil {
		log.Println("Error SQL:", err)
	}
}

/* =========================
   HANDLER
========================= */

func getLatestWeatherHandler(w http.ResponseWriter, r *http.Request) {
	var data WeatherData

	err := db.QueryRow(
		`SELECT id, timestamp, temperatura, humedad, estado_clima
		 FROM clima ORDER BY timestamp DESC LIMIT 1`,
	).Scan(
		&data.ID,
		&data.Timestamp,
		&data.Temperatura,
		&data.Humedad,
		&data.EstadoClima,
	)

	if err != nil {
		http.Error(w, "No encontrado", http.StatusNotFound)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(data)
}

/* =========================
   BANNER
========================= */

func printBanner(port string) {
	const green = "\033[32m"
	const reset = "\033[0m"
	const bold = "\033[1m"

	fmt.Printf(green + `
     _____  _     _____ __  ___  ___     ___  ________ _____ ______ _____ _____ ___________ _   _ _____ _____  ____
    /  __ \| |   |_   _|  \/  | / _ \    |  \/  |_   _/  __ \| ___ \  _  /  ___|  ___| ___ \ | | |_   _/  __ \|  __|
    | /  \/| |     | | | .  . |/ /_\ \   | .  . | | | | /  \/| |_/ / | | \ ` + "`" + `--.| |__ | |_/ / | | | | | | /  \/| |__ 
    | |    | |     | | | |\/| ||  _  |   | |\/| | | | | |    |    /| | | |` + "`" + `--. \  __||    /| | | | | | | |    |  __|
    | \__/\| |_____| |_| |  | || | | |   | |  | |_| |_| \__/\| |\ \\ \_/ /\__/ / |___| |\ \\ \_/ /_| |_| \__/\| |___
     \____/\_____/\___/\_|  |_/\_| |_/   \_|  |_/\___/ \____/\_| \_|\___/\____/\____/\_| \_|\___/ \___/ \____/\____/
` + reset)

	fmt.Printf("\n· %sCLIMA MICROSERVICE%s :: ····················%s(v0.1.0)%s\n", green, reset, bold, reset)
	fmt.Printf("· %sPowered by Go%s :: ·························%s(v%s)%s\n", green, reset, bold, runtime.Version(), reset)
	fmt.Printf("· %sPort%s :: ··································%s(%s)%s\n\n", green, reset, bold, port, reset)
}
