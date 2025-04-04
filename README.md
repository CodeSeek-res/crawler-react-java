# Cochrane Library Crawler

A web application for crawling and processing reviews from the Cochrane Library. The application consists of a Spring Boot backend for crawling and processing reviews, and a React frontend for monitoring and controlling the crawler.

## Features

- Real-time crawler status monitoring
- Start/Stop crawler functionality
- Review processing statistics
- Visual representation of crawling progress
- Error logging and monitoring
- Newly processed reviews display with animations
- Responsive design for all screen sizes

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.x
- Selenium WebDriver
- Spring Data JPA
- H2 Database
- Logback for logging

### Frontend

- React 18
- Material-UI (MUI)
- Recharts for data visualization
- Axios for API calls

## Prerequisites

- Java 17 or higher
- Node.js 16 or higher
- Maven 3.6 or higher
- Chrome browser (for Selenium WebDriver)

## Installation

### Backend Setup

1. Clone the repository:

```bash
git clone https://github.com/yourusername/cochrane.git
cd cochrane
```

2. Build the backend:

```bash
mvn clean install
```

3. Run the Spring Boot application:

```bash
mvn spring-boot:run
```

The backend server will start on http://localhost:8081

### Frontend Setup

1. Navigate to the frontend directory:

```bash
cd frontend
```

2. Install dependencies:

```bash
npm install
```

3. Start the development server:

```bash
npm start
```

The frontend application will start on http://localhost:3000

## Configuration

### Backend Configuration

The application.properties file contains important configuration settings:

```properties
server.port=8081
spring.datasource.url=jdbc:h2:file:./data/crawler
spring.jpa.hibernate.ddl-auto=update
logging.file.name=logs/crawler.log
```

### Frontend Configuration

The API base URL can be configured in src/components/CrawlerDashboard.js:

```javascript
const API_BASE_URL = "http://localhost:8081/api";
```

## API Endpoints

### Crawler Control

- `POST /api/crawler/start` - Start the crawler
- `POST /api/crawler/stop` - Stop the crawler
- `GET /api/crawler/status` - Get current crawler status

### Status Response Format

```json
{
  "running": boolean,
  "lastRun": "timestamp",
  "totalProcessed": number,
  "newReviews": [
    {
      "title": "string",
      "authors": "string",
      "topic": "string",
      "publicationDate": "date",
      "url": "string",
      "crawlStatus": "string"
    }
  ],
  "crawlingSpeed": number,
  "currentTopic": "string",
  "currentReview": "string",
  "successfulReviews": number,
  "failedReviews": number,
  "errorLog": "string"
}
```

## Logging

The application uses a comprehensive logging system:

- Application logs: `logs/crawler.log`
- API request/response logs: `logs/api.log`

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
