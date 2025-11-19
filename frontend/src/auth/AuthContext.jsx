import { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState({
    isAuthenticated: false,
    role: null,
    token: null,
    userId: null,
  });
  const [loading, setLoading] = useState(true);

  // useEffect(() => {
  //   const token = localStorage.getItem('token');
  //   const role = localStorage.getItem('role');
  //   if (token && role) {
  //     setUser({ isAuthenticated: true, role, token });
  //   }else {
  //     localStorage.clear();
  //     setUser({ isAuthenticated: false, role: null, token: null });
  //   }
  // }, []);

  useEffect(() => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");

    const fetchUserData = async () => {
      if (token && role) {
        try {
          const response = await fetch(
            "http://localhost:8089/api/v1/users/user-details",
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
          );

          if (response.ok) {
            const userData = await response.json();
            setUser({
              isAuthenticated: true,
              role,
              token,
              userData,
            });
          } else {
            // Token invalid, clear everything
            localStorage.clear();
            setUser({
              isAuthenticated: false,
              role: null,
              token: null,
              userData: null,
            });
          }
        } catch (error) {
          console.error("Error fetching user data:", error);
          localStorage.clear();
          setUser({
            isAuthenticated: false,
            role: null,
            token: null,
            userData: null,
          });
        }
      } else {
        localStorage.clear();
        setUser({
          isAuthenticated: false,
          role: null,
          token: null,
          userData: null,
        });
      }
      setLoading(false);
    };

    fetchUserData();
  }, []);

  return (
    <AuthContext.Provider value={{ user, setUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
