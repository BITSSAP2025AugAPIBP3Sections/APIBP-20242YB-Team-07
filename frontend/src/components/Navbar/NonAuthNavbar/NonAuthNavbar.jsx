import React from "react";
import { Button, Dropdown } from "antd";
import { MenuOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import "./NonAuthNavbar.css";
import { ChefHat } from "lucide-react";

const menuItems = [
  {
    key: "home",
    label: "Home",
  },
  {
    key: "contact",
    label: "Contact Us",
  },
  {
    key: "about",
    label: "About Us",
  },
  {
    key: "login",
    label: "Login",
  },
];

const NonAuthNavbar = ({ activeButton = "home" }) => {
  const navigate = useNavigate();

  const handleMenuClick = ({ key }) => {
    switch (key) {
      case "home":
        navigate("/");
        break;
      case "contact":
        navigate("/contact");
        break;
      case "about":
        navigate("/about");
        break;
      case "login":
        navigate("/login");
        break;
      default:
        break;
    }
  };

  return (
    <div className="header nonauth-navbar">
      <div className="logo">
        {/* add icons via react lucicide i.e chef-hat*/}
        <ChefHat size={32} color="#FF6F61" />
        Cooknect
      </div>
      {/* Desktop Buttons */}
      <div className="navbar-buttons">
        <Button
          className="custom-btn"
          type={activeButton === "home" ? "primary" : "default"}
          onClick={() => navigate("/")}
        >
          Home
        </Button>
        <Button
          className="custom-btn"
          type={activeButton === "contact" ? "primary" : "default"}
          onClick={() => navigate("/contact")}
        >
          Contact Us
        </Button>
        <Button
          className="custom-btn"
          type={activeButton === "about" ? "primary" : "default"}
          onClick={() => navigate("/about")}
        >
          About Us
        </Button>
        <Button
          className="custom-btn"
          type={activeButton === "login" ? "primary" : "default"}
          onClick={() => navigate("/login")}
        >
          Login
        </Button>
      </div>
      {/* Mobile/Tablet Dropdown */}
      <div className="navbar-dropdown">
        <Dropdown
          menu={{ items: menuItems, onClick: handleMenuClick }}
          placement="bottomRight"
          arrow
        >
          <Button icon={<MenuOutlined />} />
        </Dropdown>
      </div>
    </div>
  );
};

export default NonAuthNavbar;
