import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();
  if (!user.isAuthenticated || !user.token || !user.role) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

export default ProtectedRoute;