import { Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import "./NonAuthNavbar.css";

const NonAuthNavbar = ({ activeButton = 'home' }) => {
    const navigate = useNavigate();

    return (
        <div
            className="header"
            style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                background: '#fff',
                padding: '0 24px',
                boxShadow: '0 2px 8px #f0f1f2',
                marginBottom: '16px'
            }}
        >
            <div className="logo" style={{ fontWeight: 600, fontSize: 20 }}>
                Cookbook
            </div>
            <div style={{ display: 'flex', gap: '16px' }}>
                <Button
                    className="custom-btn"
                    type={activeButton === 'home' ? 'primary' : 'default'}
                    onClick={() => navigate('/')}
                >
                    Home
                </Button>
                <Button
                    className="custom-btn"
                    type={activeButton === 'contact' ? 'primary' : 'default'}
                    onClick={() => navigate('/contact')}
                >
                    Contact Us
                </Button>
                <Button
                    className="custom-btn"
                    type={activeButton === 'about' ? 'primary' : 'default'}
                    onClick={() => navigate('/about')}
                >
                    About Us
                </Button>
                <Button
                    className="custom-btn"
                    type={activeButton === 'login' ? 'primary' : 'default'}
                    onClick={() => navigate('/login')}
                >
                    Login
                </Button>
            </div>
        </div>
    );
};

export default NonAuthNavbar;