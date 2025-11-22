import React, { useState, useMemo, useEffect } from "react";
import {
  Menu,
  Dropdown,
  Avatar,
  Popover,
  Badge,
  List,
  Button,
  Tag,
  Space,
  theme,
  Typography,
} from "antd";
import { UserOutlined, LogoutOutlined, BellOutlined } from "@ant-design/icons";
import { ChefHat, Mail, Trash2, Eye } from "lucide-react";
import { useNavigate } from "react-router-dom"; // ADDED BrowserRouter for context
import { useAuth } from "../../../auth/AuthContext"; // ADDED: Importing useAuth for user context

const { Title } = Typography; // FIXED: Added destructuring for Title

// --- Static Styles ---
const styles = {
  header: (token) => ({
    display: "flex",
    alignItems: "center",
    marginBottom: token.margin,
    backgroundColor: "white",
    padding: "0 24px",
    height: "64px",
    boxShadow: token.boxShadowSecondary,
  }),
  logo: {
    marginRight: 24,
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    fontWeight: 700,
    fontSize: "1.4em",
    color: "#333",
  },
  menu: {
    flex: 1,
    minWidth: 0,
    justifyContent: "center",
    borderBottom: "none",
  },
  dropdownArea: (isHovered, token) => ({
    display: "flex",
    alignItems: "center",
    cursor: "pointer",
    padding: "0 12px",
    borderRadius: token.borderRadiusLG,
    transition: "background-color 0.3s",
    ...(isHovered ? { background: token.colorFillAlter } : {}),
  }),
  userInfo: { marginLeft: 8, fontWeight: 500, color: "black" },
  bellIcon: (token) => ({
    fontSize: "20px",
    color: token.colorTextSecondary,
    transition: "color 0.3s",
    cursor: "pointer",
    padding: token.paddingSM,
    borderRadius: token.borderRadiusLG,
    "&:hover": {
      color: token.colorPrimary,
      backgroundColor: token.colorFillAlter,
    },
  }),
  notificationItem: {
    padding: "12px 0",
    borderBottom: "1px solid #f0f0f0",
    position: "relative",
  },
  actionButtons: {
    position: "absolute",
    top: "50%",
    right: 0,
    transform: "translateY(-50%)",
    transition: "opacity 0.2s ease-in-out",
    opacity: 0, // Hidden by default
    backgroundColor: "white",
    zIndex: 10,
    paddingLeft: "8px",
  },
};

const profileDropdownItems = [
  {
    key: "profile",
    icon: <UserOutlined style={{ marginRight: "8px" }} />,
    label: "My Profile",
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
  const { token } = theme.useToken();
  const [notifications, setNotifications] = useState([]);
  const [isHovered, setIsHovered] = useState(false);
  const navigate = useNavigate();
  const { user } = useAuth();

  console.log("Schit kaise ho ", notifications);

  useEffect(() => {
    fetchNotifications();
  }, [user]);

  const fetchNotifications = async () => {
    if (!user || !user.userData) return;
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/notifications/user/${user.userData.email}`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      if (response.ok) {
        const data = await response.json();
        console.log("Fetched Notifications:", data);
        setNotifications(data);
      } else {
        console.error("Failed to fetch notifications");
      }
    } catch (error) {
      console.error("Error fetching notifications:", error);
    }
  };

  const unreadCount = useMemo(
    () => notifications.filter((n) => !n.readStatus).length,
    [notifications]
  );

  const handleMainMenuClick = ({ key }) => {
    switch (key) {
      case "1":
        navigate("/recipes"); // Assuming 1 maps to recipes
        break;
      case "3":
        navigate("/challenges");
        break;
      case "4":
        navigate("/profile");
        break;
      default:
        navigate("/");
        break;
    }
  };

  const handleDropdownClick = ({ key }) => {
    switch (key) {
      case "profile":
        navigate("/profile");
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

  const markAsRead = async (id) => {
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/notifications/${id}/read`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      if (response.ok) {
        setNotifications((prev) =>
          prev.map((n) => (n.id === id ? { ...n, readStatus: true } : n))
        );
      } else {
        console.error("Failed to mark notification as read");
      }
    } catch (error) {
      console.log(error);
    }
  };

  const deleteNotification = async (id) => {
    try {
      const response = await fetch(
        `http://localhost:8089/api/v1/notifications/${id}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );
      if (response.ok) {
        setNotifications((prev) => prev.filter((n) => n.id !== id));
      } else {
        console.error("Failed to delete notification");
      }
    } catch (error) {
      console.error("Error deleting notification:", error);
    }
  };

  const NotificationItem = ({ notification }) => {
    const [isItemHovered, setIsItemHovered] = useState(false);

    return (
      <List.Item
        key={notification.id}
        style={{
          ...styles.notificationItem,
          backgroundColor: notification.readStatus
            ? token.colorWhite
            : token.colorBgContainer,
        }}
        onMouseEnter={() => setIsItemHovered(true)}
        onMouseLeave={() => setIsItemHovered(false)}
      >
        <List.Item.Meta
          title={
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <span
                style={{ fontSize: "0.7em", color: token.colorTextTertiary }}
              >
                {new Date(notification.createdAt).toLocaleString()}
              </span>
            </div>
          }
          description={
            <p
              style={{
                margin: 0,
                color: notification.readStatus
                  ? token.colorTextTertiary
                  : token.colorTextSecondary,
              }}
            >
              {notification.body}
            </p>
          }
        />

        {/* Hover Action Buttons */}
        <div
          className="notification-actions"
          style={{ ...styles.actionButtons, opacity: isItemHovered ? 1 : 0 }}
        >
          {!notification.readStatus && (
            <Button
              size="small"
              type="text"
              icon={<Eye size={14} />}
              onClick={() => markAsRead(notification.id)}
              // tooltip="Mark as Read" // Removed tooltip property which might cause error if not imported
              style={{ color: token.colorPrimary, marginRight: "4px" }}
            />
          )}
          <Button
            size="small"
            type="text"
            icon={<Trash2 size={14} />}
            onClick={() => deleteNotification(notification.id)}
            // tooltip="Delete" // Removed tooltip property
            danger
          />
        </div>
      </List.Item>
    );
  };

  const notificationContent = (
    <div style={{ width: 350 }}>
      <List
        header={
          <Title level={5} style={{ margin: 0 }}>
            Notifications ({unreadCount} Unread)
          </Title>
        }
        itemLayout="horizontal"
        dataSource={notifications}
        renderItem={(item) => <NotificationItem notification={item} />}
        locale={{
          emptyText: (
            <span style={{ padding: "20px", color: token.colorTextTertiary }}>
              No new notifications.
            </span>
          ),
        }}
        style={{ maxHeight: "400px", overflowY: "auto" }}
      />
    </div>
  );

  return (
    <>
      <div className="header" style={styles.header(token)}>
        {/* Project Name (Logo) */}
        <div
          className="logo"
          style={styles.logo}
          onClick={() => window.location.reload()} // Navigate to root or home page
        >
          <ChefHat
            size={32}
            color={token.colorError}
            style={{ marginRight: "8px" }}
          />
          Cooknect
        </div>

        {/* Main Navigation Menu */}
        <Menu
          mode="horizontal"
          selectedKeys={[activeKey]}
          onClick={handleMainMenuClick}
          style={styles.menu}
          items={[
            { key: "1", label: "Recipes" },
            { key: "3", label: "Challenges" },
            { key: "4", label: "Profile" },
          ]}
        />

        {/* Notification Bell */}
        <Popover
          placement="bottomRight"
          content={notificationContent}
          trigger="click"
          arrow={true}
        >
          <Badge
            count={unreadCount}
            overflowCount={99}
            size="small"
            offset={[-4, 4]}
          >
            <div style={{ ...styles.bellIcon(token) }}>
              <BellOutlined />
            </div>
          </Badge>
        </Popover>

        {/* Separator */}
        <div
          style={{
            width: "1px",
            height: "32px",
            backgroundColor: token.colorBorderSecondary,
            margin: "0 16px",
          }}
        />

        {/* Avatar and Dropdown */}
        <Dropdown
          menu={{ items: profileDropdownItems, onClick: handleDropdownClick }}
          placement="bottomRight"
          arrow
        >
          <div
            style={styles.dropdownArea(isHovered, token)}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
          >
            <Avatar
              size="large"
              style={{
                backgroundColor: token.colorWarning,
                verticalAlign: "middle",
              }}
              src="https://img.freepik.com/premium-vector/cartoon-chef-with-thumbs-up-sign-that-says-thumbs-up_1166763-15878.jpg"
              alt="User Avatar"
            />
          </div>
        </Dropdown>
      </div>

      {/* This style block contains the necessary CSS for the hover effect 
        to show the action buttons on notification items.
      */}
      <style>{`
        /* Notification Hover Effect */
        .ant-list-item:hover .notification-actions {
          opacity: 1 !important;
        }
        .ant-list-item:hover {
          background-color: ${token.colorFillAlter} !important;
        }
        .ant-list-item {
          transition: background-color 0.2s;
          padding-right: 24px !important; /* Ensure space for buttons */
        }
      `}</style>
    </>
  );
};

export default LoggedInNavbar;
