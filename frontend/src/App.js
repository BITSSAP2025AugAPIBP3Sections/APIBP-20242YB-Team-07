import "./App.css";

import { BrowserRouter, Routes, Route } from "react-router-dom";
import Landing from "./pages/LandingPage/Landing";
import Home from "./pages/HomePage/Home";
import Login from "./pages/LoginPage/Login";
import Contact from "./pages/Contact/Contact";
import About from "./pages/About/About";
import Profile from "./pages/Profile/Profile";
import Recipe from "./pages/Recipe/Recipe";
import { AuthProvider } from "./auth/AuthContext";
import ProtectedRoute from "./auth/ProtectedRoute";

function App() {
  return (
    <div className="App">
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Landing />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/about" element={<About />} />
            <Route
              path="/*"
              element={
                <ProtectedRoute>
                  <Routes>
                    <Route path="/homepage" element={<Home />} />
                    <Route path="/profile/:id" element={<Profile />} />
                    <Route path="/profile" element={<Profile />} />
                    <Route path="/recipe/:id" element={<Recipe />} />
                  </Routes>
                </ProtectedRoute>
              }
            />
            <Route path="/login" element={<Login />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </div>
  );
}

export default App;
