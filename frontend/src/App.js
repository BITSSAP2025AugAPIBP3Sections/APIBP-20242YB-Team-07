import './App.css';

import { BrowserRouter, Routes, Route } from "react-router-dom";
import Landing from './pages/LandingPage/Landing';
import Home from './pages/HomePage/Home';
import Login from './pages/LoginPage/Login';

function App() {
  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Landing />} />
          <Route path="/homepage" element={<Home />} />
          <Route path="/login" element={<Login />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;
