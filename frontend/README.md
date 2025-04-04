# Cochrane Library Crawler Frontend

A modern React application for monitoring and controlling the Cochrane Library Crawler. This frontend provides a real-time dashboard for tracking crawler progress, viewing newly processed reviews, and managing the crawler's operation.

## Features

- 📊 Real-time crawler status monitoring
- 🎛️ Start/Stop crawler controls
- 📈 Live statistics dashboard
- 📑 Newly processed reviews with animations
- 📱 Fully responsive design
- 🎨 Modern Material-UI components
- 📊 Interactive data visualization with Recharts

## Tech Stack

- **React 18** - Modern UI library
- **Material-UI (MUI) 5** - Component library and styling
- **Recharts** - Data visualization
- **Axios** - API communication
- **ESLint/Prettier** - Code quality and formatting

## Getting Started

### Prerequisites

- Node.js 16 or higher
- npm 7 or higher
- Backend service running on port 8081

### Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm start
```

The application will be available at `http://localhost:3000`

### Available Scripts

- `npm start` - Runs the app in development mode
- `npm run build` - Builds the app for production
- `npm test` - Runs the test suite
- `npm run lint` - Runs ESLint to check code quality
- `npm run lint:fix` - Automatically fixes ESLint issues
- `npm run format` - Formats code using Prettier
- `npm run serve` - Serves the production build locally

## Project Structure

```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── CrawlerDashboard.js
│   │   └── [other components]
│   ├── services/
│   │   └── api.js
│   ├── utils/
│   │   └── helpers.js
│   ├── App.js
│   └── index.js
├── package.json
└── README.md
```

## Key Components

### CrawlerDashboard

The main dashboard component that displays:
- Crawler status and controls
- Processing statistics
- Review status distribution chart
- Newly processed reviews with animations

```jsx
<CrawlerDashboard />
```

### Features in Detail

#### Real-time Status Updates
- Automatic polling of crawler status
- Visual indicators for running state
- Current topic and review display

#### Statistics Display
- Total processed reviews
- Crawling speed (reviews/minute)
- Success/failure rates
- Visual distribution chart

#### Review Cards
- Animated entry for new reviews
- Detailed review information
- Direct links to source
- Status indicators

## API Integration

The frontend communicates with the backend through these endpoints:

```javascript
const API_BASE_URL = 'http://localhost:8081/api';

// Endpoints
POST /api/crawler/start  // Start the crawler
POST /api/crawler/stop   // Stop the crawler
GET  /api/crawler/status // Get current status
```

## Styling

The application uses Material-UI's styling solution with:
- Custom theme configuration
- Responsive breakpoints
- Animation keyframes
- Consistent color palette

Example of custom styling:
```jsx
<Card 
  sx={{
    transition: 'all 0.3s ease-in-out',
    animation: 'highlightNew 2s ease-out',
    '@keyframes highlightNew': {
      '0%': { backgroundColor: 'success.light' },
      '100%': { backgroundColor: 'background.paper' }
    }
  }}
>
```

## Best Practices

- ✅ Component-based architecture
- ✅ Proper error handling
- ✅ Loading states
- ✅ Responsive design
- ✅ Code splitting
- ✅ Performance optimization
- ✅ Consistent code style

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Run tests and linting before committing
4. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
5. Push to the branch (`git push origin feature/AmazingFeature`)
6. Open a Pull Request

## Development Guidelines

- Follow the existing code style
- Write meaningful commit messages
- Update documentation for new features
- Add appropriate error handling
- Test across different screen sizes
- Optimize bundle size

## Troubleshooting

### Common Issues

1. **API Connection Failed**
   - Check if the backend is running on port 8081
   - Verify CORS configuration
   - Check network connectivity

2. **Build Errors**
   - Clear npm cache: `npm cache clean --force`
   - Delete node_modules: `rm -rf node_modules`
   - Reinstall dependencies: `npm install`

3. **Development Server Issues**
   - Check for port conflicts
   - Clear browser cache
   - Update Node.js version

## License

This project is licensed under the MIT License - see the LICENSE file for details.
