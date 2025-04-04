import React from 'react';
import { Container, ThemeProvider, createTheme } from '@mui/material';
import MainDashboard from './components/MainDashboard';

const theme = createTheme();

function App() {
  return (
    <ThemeProvider theme={theme}>
      <Container>
        <MainDashboard />
      </Container>
    </ThemeProvider>
  );
}

export default App;
