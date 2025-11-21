# Feedback Collection System - React Frontend

This is the React frontend for the Feedback Collection System, designed to work with the Java backend.

## Tech Stack

- **Frontend**: React 19.1.0
- **Routing**: React Router DOM 7.6.2
- **HTTP Client**: Axios 1.9.0
- **Styling**: CSS3 with modern features
- **Build Tool**: Create React App

## Prerequisites

- Node.js 16+ and npm
- Java backend running on port 8080

## Setup Instructions

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Configure API Endpoint

The frontend is configured to connect to the Java backend at `http://localhost:8080/api`. If your backend is running on a different port or host, update the API_URL in `src/services/api.js`:

```javascript
const API_URL = 'http://localhost:8080/api';
```

### 3. Start the Development Server

```bash
npm start
```

The application will open in your browser at `http://localhost:3000`.

## Project Structure

```
frontend/
├── public/
│   ├── index.html
│   └── ...
├── src/
│   ├── components/           # Reusable components (if any)
│   ├── pages/               # Page components
│   │   ├── DashboardPage.js
│   │   ├── LoginPage.js
│   │   └── RegisterPage.js
│   ├── services/            # API services
│   │   └── api.js
│   ├── App.js              # Main App component
│   ├── App.css             # Main styles
│   └── index.js            # Entry point
├── package.json
└── README.md
```

## Features

### Authentication
- **User Registration**: Create new accounts with email validation
- **User Login**: Secure login with JWT token authentication
- **Protected Routes**: Dashboard accessible only after login
- **Logout**: Clear authentication tokens and redirect to login

### User Interface
- **Responsive Design**: Works on desktop and mobile devices
- **Modern UI**: Clean and intuitive user interface
- **Form Validation**: Client-side validation for better UX
- **Error Handling**: User-friendly error messages
- **Loading States**: Visual feedback during API calls

## API Integration

The frontend communicates with the Java backend through the following endpoints:

### Authentication Endpoints

- **POST** `/api/auth/signup` - User registration
- **POST** `/api/auth/login` - User login

### API Service

The `src/services/api.js` file handles all API communications:

```javascript
// Registration
const response = await registerUser({ name, email, password });

// Login
const response = await loginUser({ email, password });
```

## State Management

The application uses React's built-in state management:

- **Local State**: Component-level state using `useState`
- **Local Storage**: Persistent authentication tokens
- **Navigation**: React Router for client-side routing

## Styling

The application uses CSS3 with modern features:

- **CSS Variables**: For consistent theming
- **Flexbox/Grid**: For responsive layouts
- **Media Queries**: For mobile responsiveness
- **Hover Effects**: For interactive elements

## Development

### Adding New Features

1. Create new components in appropriate directories
2. Add new API calls in `src/services/api.js`
3. Update routing in `src/App.js` if needed
4. Add corresponding backend endpoints

### Environment Configuration

You can create a `.env` file in the frontend directory to override default settings:

```env
REACT_APP_API_URL=http://localhost:8080/api
```

## Building for Production

```bash
npm run build
```

This creates an optimized build in the `build/` directory.

## Deployment

### Static Hosting

The build folder can be deployed to any static hosting service:

- **Netlify**: Drag and drop the build folder
- **Vercel**: Connect your GitHub repository
- **AWS S3**: Upload the build folder contents
- **GitHub Pages**: Use the gh-pages package

### Server Deployment

For server deployment, you can serve the build folder with any web server:

```bash
# Using serve (install with: npm install -g serve)
serve -s build

# Using nginx
# Copy build folder contents to nginx html directory
```

## Testing

The project includes basic testing setup with Jest and React Testing Library:

```bash
npm test
```

## Troubleshooting

### Common Issues

1. **API Connection Error**: Ensure the Java backend is running on port 8080
2. **CORS Issues**: The Java backend includes CORS configuration
3. **Build Errors**: Clear node_modules and reinstall dependencies

### Browser Compatibility

The application supports:
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Security Considerations

- **JWT Tokens**: Stored in localStorage (consider httpOnly cookies for production)
- **Input Validation**: Both client and server-side validation
- **HTTPS**: Use HTTPS in production
- **CORS**: Properly configured for cross-origin requests

## Performance Optimization

- **Code Splitting**: React Router provides automatic code splitting
- **Bundle Analysis**: Use `npm run build` and analyze the bundle
- **Image Optimization**: Optimize images before adding to public folder
- **Lazy Loading**: Consider lazy loading for large components

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request