import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './Login';
import AuthCallback from './AuthCallback';
import Profile from './Profile';

const App = () => (
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<Login />} />
      <Route path="/auth/callbacks" element={<AuthCallback />} />
      <Route path="/profile" element={<Profile />} />
    </Routes>
  </BrowserRouter>
);

export default App;