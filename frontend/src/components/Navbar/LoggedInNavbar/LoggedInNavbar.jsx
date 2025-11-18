import "./LoggedInNavbar.css";
import { Menu, Dropdown, Avatar } from "antd";
import {
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
} from "@ant-design/icons";
import { ChefHat } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

const styles = {
  menu: {
    flex: 1,
    minWidth: 0,
    justifyContent: "center",
    borderBottom: "none",
  },
  dropdownArea: {
    display: "flex",
    alignItems: "center",
    cursor: "pointer",
    padding: "0 12px",
  },
  dropdownHover: { background: "#f5f5f5" },
  userInfo: { marginLeft: 8, fontWeight: 500, color: "black" },
};

const items = [
  {
    key: "profile",
    icon: <UserOutlined style={{ marginRight: "8px" }} />,
    label: "My Profile",
  },
  {
    key: "settings",
    icon: <SettingOutlined style={{ marginRight: "8px" }} />,
    label: "Settings",
  },
  {
    type: "divider",
  },
  {
    key: "logout",
    icon: <LogoutOutlined style={{ marginRight: "8px" }} />,
    label: "Logout",
    danger: true,
  },
];

const LoggedInNavbar = ({ activeKey = "1" }) => {
  const [activeMenuKey, setActiveMenuKey] = useState("1");
  const [isHovered, setIsHovered] = useState(false);
  const navigate = useNavigate();

  const handleMainMenuClick = ({ key }) => {
    setActiveMenuKey(key);
    switch (key) {
      case "1":
        navigate("/homepage");
        break;
      case "2":
        navigate("/recipe");
        break;
      case "3":
        navigate("/browse");
        break;
      case "4":
        navigate("/profile");
        break;
      default:
        break;
    }
  };

  const handleDropdownClick = ({ key }) => {
    switch (key) {
      case "profile":
        navigate("/profile");
        setActiveMenuKey("0");
        break;
      case "settings":
        navigate("/settings");
        setActiveMenuKey("0");
        break;
      case "logout":
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
        break;
      default:
        break;
    }
  };

  return (
    <div
      className="header"
      style={{ display: "flex", alignItems: "center", marginBottom: "16px" }}
    >
      {/* Project Name (Logo) */}
      <div
        className="logo"
        style={{ marginRight: 24, cursor: "pointer" }}
        onClick={() => navigate("/homepage")}
      >
        <ChefHat size={32} color="#FF6F61" /> Cooknect
      </div>

      {/* Main Navigation Menu */}
      <Menu
        mode="horizontal"
        selectedKeys={[activeKey]}
        onClick={handleMainMenuClick}
        style={styles.menu}
        items={[
          { key: "1", label: "Recipes" },
          { key: "2", label: "My Recipes" },
          { key: "3", label: "Browse" },
          { key: "4", label: "Profile" },
        ]}
      />

      {/* Avatar and Dropdown */}
      <Dropdown
        menu={{ items, onClick: handleDropdownClick }}
        placement="bottomRight"
        arrow
      >
        <div
          style={{
            ...styles.dropdownArea,
            ...(isHovered ? styles.dropdownHover : {}),
          }}
          onMouseEnter={() => setIsHovered(true)}
          onMouseLeave={() => setIsHovered(false)}
        >
          <Avatar
            size="large"
            style={{ backgroundColor: "#f56a00", verticalAlign: "middle" }}
            src="https://img.freepik.com/premium-vector/cartoon-chef-with-thumbs-up-sign-that-says-thumbs-up_1166763-15878.jpg"
          />
        </div>
      </Dropdown>
    </div>
  );
};

export default LoggedInNavbar;
