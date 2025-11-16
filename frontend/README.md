# Spring Demo Frontend - Angular Application

This is the frontend application for the Spring Boot demo project, built with Angular 17.

## Project Structure

```
frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── home/           # Home page component
│   │   │   └── demo/           # API demo component
│   │   ├── services/
│   │   │   └── api.service.ts  # HTTP service for backend API
│   │   ├── app.component.*     # Root component
│   │   ├── app.module.ts       # Root module
│   │   └── app-routing.module.ts  # Routing configuration
│   ├── environments/           # Environment configurations
│   ├── assets/                 # Static assets
│   ├── index.html              # Main HTML file
│   ├── main.ts                 # Application entry point
│   └── styles.css              # Global styles
├── angular.json                # Angular CLI configuration
├── tsconfig.json               # TypeScript configuration
├── package.json                # NPM dependencies
└── proxy.conf.json             # Proxy configuration for backend API
```

## Prerequisites

- Node.js 18.x or higher
- npm 9.x or higher
- Angular CLI 17.x

## Installation

1. Install dependencies:

```bash
cd frontend
npm install
```

2. Install Angular CLI globally (if not already installed):

```bash
npm install -g @angular/cli
```

## Development Server

Run the development server:

```bash
npm start
```

Or using Angular CLI:

```bash
ng serve
```

Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

## Building for Production

Build the project:

```bash
npm run build
```

Or using Angular CLI:

```bash
ng build --configuration production
```

The build artifacts will be stored in the `dist/` directory.

## Backend Integration

The frontend is configured to communicate with the Spring Boot backend running on `http://localhost:8080`.

### Proxy Configuration

During development, API requests are proxied through Angular's dev server using `proxy.conf.json`:

- All requests to `/api/*` are forwarded to `http://localhost:8080/api/*`
- This avoids CORS issues during development

### API Endpoints Used

1. **Health Check**
   - Endpoint: `GET /api/health`
   - Description: Check if the backend is running

2. **Hello API**
   - Endpoint: `GET /api/hello?name={name}`
   - Description: Get a personalized greeting message

## Features

### Home Page
- Welcome page with feature overview
- Responsive design
- Navigation to other pages

### API Demo Page
- Interactive testing of backend API endpoints
- Real-time health status check
- Personalized greeting functionality
- Error handling and loading states

## Running with Backend

1. Start the Spring Boot backend:
   ```bash
   cd ..
   ./gradlew bootRun
   ```

2. In a separate terminal, start the Angular frontend:
   ```bash
   cd frontend
   npm start
   ```

3. Open your browser and navigate to:
   - Frontend: `http://localhost:4200`
   - Backend: `http://localhost:8080`

## Architecture

### Components

- **AppComponent**: Root component with navigation
- **HomeComponent**: Landing page with features
- **DemoComponent**: Interactive API testing page

### Services

- **ApiService**: Handles all HTTP requests to the backend
  - Uses Angular HttpClient
  - Implements error handling
  - Returns RxJS Observables

### Routing

- `/` - Home page
- `/demo` - API demo page
- Lazy-loaded modules for better performance

## Styling

- Custom CSS with responsive design
- Mobile-friendly layout
- Clean, modern UI with card-based design
- Smooth animations and transitions

## Testing

Run unit tests:

```bash
npm test
```

Or using Angular CLI:

```bash
ng test
```

## Code Scaffolding

Generate a new component:

```bash
ng generate component component-name
```

Generate a new service:

```bash
ng generate service service-name
```

## Further Help

For more help on Angular CLI:

```bash
ng help
```

Or visit the [Angular Documentation](https://angular.io/docs).

## Technologies Used

- **Angular 17** - Frontend framework
- **TypeScript 5.4** - Programming language
- **RxJS 7.8** - Reactive programming library
- **Angular Router** - Navigation and routing
- **HttpClient** - HTTP communication
- **CSS3** - Styling and animations

## Development Tips

1. **Hot Reload**: Changes to TypeScript/HTML/CSS files trigger automatic reload
2. **Proxy Issues**: If API calls fail, check that the backend is running on port 8080
3. **CORS**: The proxy configuration handles CORS during development
4. **Build Optimization**: Production builds are optimized and minified
5. **Lazy Loading**: Routes are lazy-loaded for better initial load performance

## License

This is a demo application for educational purposes.
