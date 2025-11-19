import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useAuth();
  if (loading) {
    return <div>Loading...</div>; // Or your loading component
  }
  if (!user.isAuthenticated || !user.token || !user.role) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

export default ProtectedRoute;