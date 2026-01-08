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

var cities = []City{
	{Name: "BuenosAires", Lat: -34.6037, Lon: -58.3816},
}

func main() {
	// Cargar .env en local (Railway lo ignora)
	if err := godotenv.Load(); err != nil {
		log.Println("Aviso: no se encontró .env, usando entorno")
	}

	// Construir DATABASE_URL unificada
	dbURL := fmt.Sprintf(
		"postgresql://%s:%s@%s:%s/%s?sslmode=disable",
		os.Getenv("POSTGRES_USER"),
		os.Getenv("POSTGRES_PASS"),
		os.Getenv("POSTGRES_HOST"),
		os.Getenv("POSTGRES_PORT"),
		os.Getenv("POSTGRES_DB"),
	)

	var err error
	db, err = sql.Open("postgres", dbURL)
	if err != nil {
		log.Fatal("Error abriendo DB:", err)
	}
	defer db.Close()

	if err := db.Ping(); err != nil {
		log.Fatal("No se pudo conectar a la DB:", err)
	}

	// Worker horario exacto
	go hourlyWorker()

	http.HandleFunc("/clima/latest", getLatestWeatherHandler)

	port := os.Getenv("PORT")
	if port == "" {
		port = "8080"
	}

	printBanner(port)

	log.Println("Servidor iniciado con éxito")
	log.Fatal(http.ListenAndServe(":"+port, nil))
}

func hourlyWorker() {
	for {
		now := time.Now()
		next := now.Truncate(time.Hour).Add(time.Hour)
		wait := time.Until(next)

		time.Sleep(wait)

		for _, city := range cities {
			updateWeather(city)
		}
	}
}

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

	_, err = db.Exec(
		`INSERT INTO clima (timestamp, temperatura, humedad, estado_clima)
		 VALUES ($1, $2, $3, $4)`,
		time.Now(),
		result.Main.Temp,
		result.Main.Humidity,
		estado,
	)

	if err != nil {
		log.Println("Error SQL:", err)
		return
	}

}

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

func printBanner(port string) {
	const green = "\033[32m"
	const reset = "\033[0m"
	const bold = "\033[1m"

	fmt.Printf(green + `
     _____  _     _____ __  ___  ___     ___  ________ _____ ______ _____ _____ ___________ _   _ _____ _____  _____
    /  __ \| |   |_   _|  \/  | / _ \    |  \/  |_   _/  __ \| ___ \  _  /  ___|  ___| ___ \ | | |_   _/  __ \|  ___|
    | /  \/| |     | | | .  . |/ /_\ \   | .  . | | | | /  \/| |_/ / | | \ ` + "`" + `--.| |__ | |_/ / | | | | | | /  \/| |__ 
    | |    | |     | | | |\/| ||  _  |   | |\/| | | | | |    |    /| | | |` + "`" + `--. \  __||    /| | | | | | | |    |  __|
    | \__/\| |_____| |_| |  | || | | |   | |  | |_| |_| \__/\| |\ \\ \_/ /\__/ / |___| |\ \\ \_/ /_| |_| \__/\| |___
     \____/\_____/\___/\_|  |_/\_| |_/   \_|  |_/\___/ \____/\_| \_|\___/\____/\____/\_| \_|\___/ \___/ \____/\____/
` + reset)

	fmt.Printf("\n· %sCLIMA MICROSERVICE%s :: ····················%s(v0.1.0)%s\n", green, reset, bold, reset)
	fmt.Printf("· %sPowered by Go%s :: ·························%s(v%s)%s\n", green, reset, bold, runtime.Version(), reset)
	fmt.Printf("· %sPort%s :: ··································%s(%s)%s\n\n", green, reset, bold, port, reset)
}

