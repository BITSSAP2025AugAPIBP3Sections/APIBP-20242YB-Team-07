import React, { useState, useEffect } from "react";
import {
  Layout,
  Typography,
  Button,
  Card,
  Row,
  Col,
  theme,
  Space,
  Divider,
  Progress,
  Tag,
  List,
  Statistic,
  notification,
  Modal,
  Form,
  Input,
  DatePicker,
  Select,
  InputNumber,
  message,
  Spin,
  Pagination,
} from "antd";
import dayjs from "dayjs";
import {
  Rocket,
  Flame,
  Users,
  Clock,
  ArrowLeft,
  CheckCircle,
  Star,
  Trophy,
  Award,
  Plus,
} from "lucide-react";
import { useAuth } from "../../auth/AuthContext";
import LoggedInNavbar from "../../components/Navbar/LoggedInNavbar/LoggedInNavbar";

const { Header, Content } = Layout;
const { Title, Paragraph, Text } = Typography;
const { Option } = Select;

const API_BASE_URL = "http://localhost:8089/api/v1/challenges";
const MAX_RETRIES = 3;
const BASE_DELAY_MS = 1000;

const fetchWithBackoff = async (url, options = {}, retries = 0) => {
  try {
    const response = await fetch(url, options);
    // Check for HTTP status codes that indicate an error, including 4xx and 5xx
    if (!response.ok) {
      // Throw error with status to differentiate successful vs bad responses
      const errorMessage = await response
        .text()
        .catch(() => response.statusText);
      throw new Error(
        `HTTP error! Status: ${response.status} - ${errorMessage}`,
        { cause: response.status }
      );
    }
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
      return await response.json();
    }
    // Return the full response object for non-JSON responses (like ok(result) with text)
    return response;
  } catch (err) {
    if (retries < MAX_RETRIES) {
      const delay = BASE_DELAY_MS * Math.pow(2, retries) + Math.random() * 500;
      console.warn(
        `Fetch failed for ${url}. Retrying in ${delay.toFixed(
          0
        )}ms... (Attempt ${retries + 1}/${MAX_RETRIES})`
      );
      await new Promise((resolve) => setTimeout(resolve, delay));
      return fetchWithBackoff(url, options, retries + 1);
    }
    throw err;
  }
};

const EmptyChallengeData = [];

const getDifficultyColor = (difficulty) => {
  switch (difficulty) {
    case "Easy":
      return { color: "#52c41a", tag: "green" };
    case "Medium":
      return { color: "#faad14", tag: "orange" };
    case "Hard":
      return { color: "#f5222d", tag: "red" };
    default:
      return { color: "#d9d9d9", tag: "default" };
  }
};

const getCostTag = (isPaid, entryFee) => {
  if (!isPaid) {
    return (
      <Tag color="green" style={{ padding: "4px 8px", fontSize: "13px" }}>
        FREE
      </Tag>
    );
  }
  return (
    <Tag color="volcano" style={{ padding: "4px 8px", fontSize: "13px" }}>
      ₹{entryFee?.toFixed(2) || "N/A"}
    </Tag>
  );
};

const parseTotalDays = (durationStr) => {
  const match = durationStr?.match(/(\d+)\s+Days/i);
  return match ? parseInt(match[1], 10) : 0;
};

const CustomCardTitle = ({ title, difficulty }) => {
  const { color } = getDifficultyColor(difficulty);
  return (
    <Space direction="vertical" size={4} style={{ width: "100%" }}>
      {difficulty && (
        <Text
          style={{
            fontSize: "12px",
            color: color,
            fontWeight: "600",
            textTransform: "uppercase",
          }}
        >
          {difficulty}
        </Text>
      )}
      <Title
        level={4}
        style={{ margin: 0, fontWeight: "700", color: "#264653" }}
      >
        {title}
      </Title>
    </Space>
  );
};

const CardStyle = (token) => ({
  borderRadius: "16px",
  overflow: "hidden",
  cursor: "pointer",
  transition: "all 0.3s ease-in-out",
  boxShadow: token.boxShadowSecondary,
  "&:hover": {
    transform: "translateY(-4px)",
    boxShadow: token.boxShadowTertiary,
  },
});

const ChallengeListPage = ({
  challenges,
  onViewDetails,
  token,
  totalChallenges,
  currentPage,
  fetchChallenges,
  handleJoinOrLeaveChallenge,
  joiningChallengeId,
  userChallengeStatus,
  paymentPending,
  onAddRecipe,
  participantsCount,
}) => {
  if (challenges.length === 0 && totalChallenges === 0) {
    return (
      <div style={{ textAlign: "center", padding: "100px 0" }}>
        <Title level={3} type="secondary">
          No Challenges Found
        </Title>
        <Paragraph>
          It looks like there are no active challenges right now. Try creating
          one!
        </Paragraph>
      </div>
    );
  }

  const computeDateProgress = (challenge) => {
    if (!challenge?.startDate || !challenge?.endDate) {
      return {
        percent: 0,
        remainingDays: null,
        totalDays: null,
        elapsedDays: 0,
      };
    }
    const start = dayjs(challenge.startDate);
    const end = dayjs(challenge.endDate);
    const now = dayjs();

    if (!start.isValid() || !end.isValid() || end.isBefore(start)) {
      return {
        percent: 0,
        remainingDays: null,
        totalDays: null,
        elapsedDays: 0,
      };
    }

    const totalDays = end.diff(start, "day");
    if (totalDays <= 0)
      return { percent: 100, remainingDays: 0, totalDays: 0, elapsedDays: 0 };
    const elapsedDays = Math.min(
      Math.max(now.diff(start, "day"), 0),
      totalDays
    );
    const remainingDays = Math.max(totalDays - elapsedDays, 0);
    const percent = Math.min(100, Math.round((elapsedDays / totalDays) * 100));

    return { percent, remainingDays, totalDays, elapsedDays };
  };

  return (
    <div className="challenge-list-container">
      <div style={{ marginTop: 40, marginBottom: 20 }}>
        <Title level={2} style={{ color: token.colorPrimary, fontWeight: 800 }}>
          <Flame
            size={32}
            style={{ marginBottom: "-5px", marginRight: "8px" }}
          />
          Explore Culinary Challenges
        </Title>
        <Paragraph type="secondary">
          Ignite your passion for cooking and recipe creation by joining one of
          our challenges.
        </Paragraph>
        <Divider />
      </div>

      <Row gutter={[32, 32]} style={{ padding: "0 0 40px 0" }}>
        {challenges.map((challenge) => {
          const dp = computeDateProgress(challenge);
          const joining = joiningChallengeId === challenge.id;
          // Determine if the user has joined based on the new state
          const isJoined = userChallengeStatus[challenge.id] === "JOINED";
          const buttonDisabled = dp.percent === 100 || joining;

          // Determine button text based on status and progress
          let buttonText;
          if (dp.percent === 100) {
            buttonText = "Challenge Completed!";
          } else if (joining) {
            buttonText = isJoined ? "Leaving..." : "Joining...";
          } else if (isJoined) {
            buttonText = "Leave Challenge";
          } else {
            // If payment is pending for this challenge, show verify button
            if (paymentPending[challenge.id]) {
              buttonText = "Verify Payment";
            } else {
              buttonText = challenge.isPaid
                ? `Enroll for ₹${challenge.entryFee?.toFixed(2) || "0.00"}`
                : "Join Challenge";
            }
          }

          return (
            <Col xs={24} md={12} lg={8} key={challenge.id}>
              <Card
                hoverable
                style={CardStyle(token)}
                cover={
                  <div style={{ height: "200px", overflow: "hidden" }}>
                    <img
                      alt={challenge.title}
                      src={
                        challenge.image ||
                        `https://placehold.co/600x400/A8E6CF/000000?text=${encodeURIComponent(
                          challenge.name || challenge.title || "Challenge"
                        )}`
                      }
                      style={{
                        width: "100%",
                        height: "100%",
                        objectFit: "cover",
                      }}
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = `https://placehold.co/600x400/A8E6CF/000000?text=${encodeURIComponent(
                          challenge.name || challenge.title || "Challenge"
                        )}`;
                      }}
                    />
                  </div>
                }
                onClick={() => onViewDetails(challenge.id)}
              >
                <Card.Meta
                  title={
                    <CustomCardTitle
                      title={challenge.title}
                      difficulty={challenge.difficulty}
                    />
                  }
                  description={
                    <>
                      <Space
                        size={[0, 8]}
                        wrap
                        style={{ marginBottom: "12px" }}
                      >
                        <Tag icon={<Flame size={14} />} color="volcano">
                          {challenge.category || "General"}
                        </Tag>
                        <Tag icon={<Clock size={14} />} color="default">
                          {(dp.totalDays ?? 0) + " Days"}
                        </Tag>
                      </Space>
                      <Paragraph
                        ellipsis={{ rows: 2 }}
                        style={{ color: token.colorTextSecondary }}
                      >
                        {challenge.description}
                      </Paragraph>

                      <Divider style={{ margin: "16px 0 8px 0" }} />

                      <Space
                        direction="vertical"
                        size={4}
                        style={{ width: "100%" }}
                      >
                        <Space
                          size="large"
                          style={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                          }}
                        >
                          <Text type="secondary" style={{ fontSize: "12px" }}>
                            <Users size={14} style={{ marginBottom: "-3px" }} />{" "}
                            {participantsCount[challenge.id] ??
                              challenge.participants ??
                              0}{" "}
                            chefs joined
                          </Text>
                          {getCostTag(challenge?.isPaid, challenge?.entryFee)}
                        </Space>

                        <Progress
                          percent={dp.percent}
                          size="small"
                          status={dp.percent === 100 ? "success" : "active"}
                          strokeColor={token.colorSuccess}
                        />
                        <Text type="secondary" style={{ fontSize: 11 }}>
                          {dp.percent === 100
                            ? "Completed"
                            : `${dp.elapsedDays}d done • ${dp.remainingDays}d left (${dp.percent}%)`}
                        </Text>

                        {/* Button for Join/Leave */}
                        <div style={{ marginTop: 12 }}>
                          <Button
                            type={isJoined ? "default" : "primary"}
                            danger={isJoined} // Use danger style for leaving
                            loading={joining}
                            disabled={buttonDisabled}
                            icon={
                              isJoined ? null : dp.percent === 100 ? (
                                <CheckCircle size={18} />
                              ) : paymentPending[challenge.id] ? null : (
                                <Rocket size={18} />
                              )
                            }
                            block
                            onClick={(e) => {
                              e.stopPropagation();
                              handleJoinOrLeaveChallenge(
                                challenge.id,
                                challenge.isPaid,
                                isJoined
                              );
                            }}
                          >
                            {buttonText}
                          </Button>
                        </div>
                        {/* End Button */}
                      </Space>
                    </>
                  }
                />
              </Card>
            </Col>
          );
        })}
      </Row>

      <Pagination
        current={currentPage}
        total={totalChallenges}
        pageSize={6}
        onChange={(pageNumber) => fetchChallenges(pageNumber, 6)}
        style={{ display: "flex", justifyContent: "center", marginTop: "20px" }}
      />
    </div>
  );
};

const ChallengeDetailPage = ({
  challenge,
  onBackToList,
  token,
  handleJoinOrLeaveChallenge,
  joiningChallengeId,
  userChallengeStatus,
  onAddRecipe,
  participantsCount,
}) => {
  // Leaderboard state fetched when detail page opens
  const [leaderboardLoading, setLeaderboardLoading] = useState(false);
  const [leaderboardData, setLeaderboardData] = useState([]);
  const [leaderboardError, setLeaderboardError] = useState(null);

  useEffect(() => {
    const loadLeaderboard = async () => {
      setLeaderboardLoading(true);
      setLeaderboardError(null);
      try {
        const resp = await fetchWithBackoff(
          `${API_BASE_URL}/${challenge.id}/leaderboard`,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: `Bearer ${localStorage.getItem("token")}`,
            },
          }
        );
        const entries = Array.isArray(resp) ? resp : resp?.leaderboard || [];
        setLeaderboardData(entries);
      } catch (err) {
        console.error("Failed to load leaderboard:", err);
        setLeaderboardError(err.message || "Failed to load leaderboard");
      } finally {
        setLeaderboardLoading(false);
      }
    };

    if (challenge?.id) {
      loadLeaderboard();
    }
  }, [challenge?.id]);

  // If challenge is missing, render not-found after hooks are declared
  if (!challenge)
    return (
      <div style={{ textAlign: "center", padding: "100px 0" }}>
        <Title level={3} type="danger">
          Challenge Not Found
        </Title>
        <Button onClick={onBackToList} icon={<ArrowLeft size={18} />}>
          Back to Challenges
        </Button>
      </div>
    );

  const { color: diffColor } = getDifficultyColor(challenge.difficulty);
  // Compute date-based progress like list page
  const computeDateProgressDetail = () => {
    if (!challenge?.startDate || !challenge?.endDate) {
      return {
        percent: 0,
        remainingDays: null,
        totalDays: null,
        elapsedDays: 0,
      };
    }
    const start = dayjs(challenge.startDate);
    const end = dayjs(challenge.endDate);
    const now = dayjs();

    if (!start.isValid() || !end.isValid() || end.isBefore(start)) {
      return {
        percent: 0,
        remainingDays: null,
        totalDays: null,
        elapsedDays: 0,
      };
    }

    const totalDays = end.diff(start, "day");
    if (totalDays <= 0)
      return { percent: 100, remainingDays: 0, totalDays: 0, elapsedDays: 0 };
    const elapsedDays = Math.min(
      Math.max(now.diff(start, "day"), 0),
      totalDays
    );
    const remainingDays = Math.max(totalDays - elapsedDays, 0);
    const percent = Math.min(100, Math.round((elapsedDays / totalDays) * 100));

    return { percent, remainingDays, totalDays, elapsedDays };
  };
  const dpDetail = computeDateProgressDetail();

  const joining = joiningChallengeId === challenge.id;
  const isJoined = userChallengeStatus[challenge.id] === "JOINED";
  const buttonDisabled = dpDetail.percent === 100 || joining;

  let buttonText;
  if (dpDetail.percent === 100) {
    buttonText = "Challenge Completed!";
  } else if (joining) {
    buttonText = isJoined ? "Leaving..." : "Joining...";
  } else if (isJoined) {
    buttonText = "Leave Challenge";
  } else {
    buttonText = challenge.isPaid
      ? `Enroll for ₹${challenge.entryFee?.toFixed(2) || "0.00"}`
      : "Join Challenge";
  }

  return (
    <div style={{ maxWidth: "900px", margin: "0 auto", padding: "40px 0" }}>
      <Button
        type="link"
        icon={<ArrowLeft size={18} />}
        onClick={onBackToList}
        style={{ marginBottom: "24px", paddingLeft: 0, fontWeight: "600" }}
      >
        Back to All Challenges
      </Button>

      <Card
        style={{
          borderRadius: "20px",
          padding: "0",
          boxShadow: token.boxShadowTertiary,
        }}
      >
        <div
          style={{
            height: "350px",
            overflow: "hidden",
            borderRadius: "20px 20px 0 0",
          }}
        >
          <img
            alt={challenge.title}
            src={
              challenge.image ||
              `https://placehold.co/900x350/A8E6CF/000000?text=${encodeURIComponent(
                challenge.name || challenge.title || "Challenge"
              )}`
            }
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
            onError={(e) => {
              e.target.onerror = null;
              e.target.src = `https://placehold.co/900x350/A8E6CF/000000?text=${encodeURIComponent(
                challenge.name || challenge.title || "Challenge"
              )}`;
            }}
          />
        </div>

        <div style={{ padding: "30px 40px" }}>
          <Row justify="space-between" align="middle">
            <Col>
              <Title
                level={2}
                style={{ margin: 0, fontWeight: "800", color: "#264653" }}
              >
                {challenge.title}
              </Title>
            </Col>
            <Col>{getCostTag(challenge?.isPaid, challenge?.entryFee)}</Col>
          </Row>

          <Space
            size="large"
            style={{ marginTop: "16px", marginBottom: "24px" }}
            wrap
          >
            {challenge.difficulty && (
              <Tag
                color={getDifficultyColor(challenge.difficulty).tag}
                style={{ padding: "6px 12px", fontSize: "14px" }}
              >
                <Star
                  size={14}
                  style={{ marginBottom: "-2px", marginRight: "4px" }}
                />{" "}
                Skill Level: {challenge.difficulty}
              </Tag>
            )}
            <Text strong>
              <Clock size={16} style={{ marginBottom: "-3px" }} /> Duration:{" "}
              {(dpDetail.totalDays ?? 0) + " Days"}
            </Text>
            <Text strong>
              <Users size={16} style={{ marginBottom: "-3px" }} />{" "}
              {(
                participantsCount[challenge.id] ??
                challenge.participants ??
                0
              ).toLocaleString()}{" "}
              Chefs
            </Text>
            <Text strong>
              Opening date:{" "}
              {challenge.startDate
                ? dayjs(challenge.startDate).format("YYYY-MM-DD")
                : "N/A"}
            </Text>
            <Text strong>
              Closing date:{" "}
              {challenge.endDate
                ? dayjs(challenge.endDate).format("YYYY-MM-DD")
                : "N/A"}
            </Text>
          </Space>

          <Divider />

          <Title
            level={3}
            style={{
              color: token.colorText,
              marginTop: "24px",
              fontWeight: 800,
            }}
          >
            Challenge Goal
          </Title>
          <Paragraph style={{ fontSize: "1.05em", lineHeight: "1.7" }}>
            {challenge.description}
          </Paragraph>

          {/* Progress & Action */}
          <div
            style={{
              margin: "30px 0",
              padding: "20px",
              border: `1px solid ${diffColor}`,
              borderRadius: "12px",
              backgroundColor: token.colorFillAlter,
            }}
          >
            <Text
              strong
              style={{
                display: "block",
                marginBottom: "8px",
                color: token.colorPrimary,
              }}
            >
              Days Left:{" "}
              <Text style={{ color: diffColor }}>
                {dpDetail.totalDays ?? 0} Days
              </Text>
            </Text>
            <Row align="middle" gutter={24}>
              <Col xs={24} sm={18}>
                <Progress
                  percent={dpDetail.percent}
                  status={dpDetail.percent === 100 ? "success" : "active"}
                  strokeColor={token.colorSuccess}
                  showInfo={false}
                />
                <div
                  style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    marginTop: 6,
                  }}
                >
                  <Text style={{ color: token.colorTextSecondary }}>
                    {dpDetail.percent === 100
                      ? "Completed"
                      : `${dpDetail.elapsedDays}d done • ${dpDetail.remainingDays}d left`}
                  </Text>
                  <Text strong style={{ color: token.colorSuccess }}>
                    ({dpDetail.percent || 0}%)
                  </Text>
                </div>
              </Col>
              <Col xs={24} sm={6}>
                <Button
                  type={isJoined ? "default" : "primary"}
                  danger={isJoined} // Use danger style for leaving
                  size="large"
                  loading={joining}
                  icon={
                    isJoined ? null : dpDetail.percent === 100 ? (
                      <CheckCircle size={18} />
                    ) : (
                      <Rocket size={18} />
                    )
                  }
                  block
                  disabled={buttonDisabled}
                  onClick={() =>
                    handleJoinOrLeaveChallenge(
                      challenge.id,
                      challenge.isPaid,
                      isJoined
                    )
                  }
                >
                  {buttonText}
                </Button>
              </Col>
            </Row>
          </div>

          {/* Add Recipe (moved to replace Rewards & Recognition) */}
          {isJoined && dpDetail.percent !== 100 && (
            <div style={{ marginTop: "30px" }}>
              <Button
                type="primary"
                block
                onClick={() => onAddRecipe && onAddRecipe(challenge.id)}
              >
                Add Recipe
              </Button>
            </div>
          )}

          <Divider style={{ margin: "40px 0 20px 0" }} />

          {/* Leaderboard Section */}
          <Title
            level={3}
            style={{ color: token.colorText, marginTop: "30px" }}
          >
            <Award
              size={24}
              style={{ marginBottom: "-5px", marginRight: "8px" }}
            />{" "}
            Challenge Leaderboard
          </Title>
          {leaderboardLoading ? (
            <div style={{ padding: "16px" }}>
              <Spin />
            </div>
          ) : leaderboardError ? (
            <Text type="danger">{leaderboardError}</Text>
          ) : (
            <List
              itemLayout="horizontal"
              dataSource={leaderboardData}
              style={{
                marginTop: "16px",
                backgroundColor: token.colorBgContainer,
                padding: "16px",
                borderRadius: "8px",
              }}
              renderItem={(item, index) => (
                <List.Item>
                  <List.Item.Meta
                    avatar={
                      <Statistic
                        title="Rank"
                        value={index + 1}
                        valueStyle={{
                          color:
                            index + 1 === 1
                              ? token.colorSuccess
                              : token.colorPrimary,
                          fontWeight: "bold",
                        }}
                        prefix={<Text style={{ fontSize: 14 }}>#</Text>}
                      />
                    }
                    title={
                      <Text
                        strong
                        style={{ fontSize: "1.1em", color: token.colorText }}
                      >
                        {index + 1 <= 3 && (
                          <Trophy
                            size={16}
                            style={{
                              marginBottom: "-3px",
                              marginRight: "4px",
                              color:
                                index + 1 === 1
                                  ? "#FFD700"
                                  : index + 1 === 2
                                  ? "#C0C0C0"
                                  : "#CD7F32",
                            }}
                          />
                        )}
                        {item.username || item.name || item.userId}
                      </Text>
                    }
                    description={`Submitted ${
                      item.recipeCount || 0
                    } recipes • ${item.totalLikes || 0} likes`}
                  />
                  <div>
                    <Text strong style={{ color: diffColor }}>
                      {item.totalLikes || 0} Likes
                    </Text>
                  </div>
                </List.Item>
              )}
            />
          )}

          <Divider style={{ margin: "40px 0 20px 0" }} />

          {/* Focus Areas removed as requested */}
        </div>
      </Card>
    </div>
  );
};

const Challenge = () => {
  const { token } = theme.useToken();
  const [form] = Form.useForm();
  const isPaid = Form.useWatch("isPaid", form);
  const { user } = useAuth();
  const userId = user?.userData?.id;
  const [joiningChallengeId, setJoiningChallengeId] = useState(null);
  // State to track the user's join status for each challenge
  const [userChallengeStatus, setUserChallengeStatus] = useState({});
  // Track pending payment for paid challenges: { [challengeId]: paymentId }
  const [paymentPending, setPaymentPending] = useState({});
  // Track participants count per challenge: { [challengeId]: number }
  const [participantsCount, setParticipantsCount] = useState({});

  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 6;

  // Set a mock token for testing the API calls in a mock environment
  // useEffect(() => {
  //   if (!localStorage.getItem('token')) {
  //       localStorage.setItem('token', 'mock-auth-token-for-api');
  //   }
  // }, []);

  const initialValues = {
    isPaid: false,
    entryFee: 0,
    type: "MOST_RECIPES",
    startDate: dayjs().add(1, "day").startOf("day"),
    endDate: dayjs().add(8, "days").startOf("day"),
  };
  const disabledDate = (current) => {
    return current && current < dayjs().endOf("day");
  };

  const [activeChallengeId, setActiveChallengeId] = useState(null);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [challenges, setChallenges] = useState([]);
  // Submit recipe modal state
  const [isRecipeModalVisible, setIsRecipeModalVisible] = useState(false);
  const [submitRecipeChallengeId, setSubmitRecipeChallengeId] = useState(null);
  const [recipeForm] = Form.useForm();
  // Local UI-driven payment error modal (ensures visibility)
  const [paymentErrorModal, setPaymentErrorModal] = useState({
    open: false,
    message: "",
  });

  const activeChallenge = challenges.find((c) => c.id === activeChallengeId);

  const handleViewDetails = (id) => {
    setActiveChallengeId(id);
  };

  const handleBackToList = () => {
    setActiveChallengeId(null);
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
    // Optional: Scroll to top of list when page changes
    const listTop = document.querySelector(".challenge-list-container");
    if (listTop) {
      // Scroll the view into sight, adjusted for header height if possible
      window.scrollTo({ top: listTop.offsetTop - 80, behavior: "smooth" });
    } else {
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  };

  const [isLoading, setIsLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const fetchChallenges = async (page = 1, size = pageSize) => {
    setIsLoading(true);
    try {
      const data = await fetchWithBackoff(
        `${API_BASE_URL}?page=${page}&size=${size}&sortBy=id&direction=asc`,
        {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      // Assuming API returns content, totalPages, totalElements
      setChallenges(data.content || EmptyChallengeData);
      setTotalPages(data.totalPages ?? 1);
      setTotalElements(data.totalElements ?? data.content?.length ?? 0);
      setCurrentPage((data.number ?? page - 1) + 1);

      // After loading challenges, fetch participants list for counts and user membership
      if (Array.isArray(data.content)) {
        const tokenStr = localStorage.getItem("token");
        const statusEntries = await Promise.all(
          data.content.map(async (c) => {
            try {
              const participantsUrl = `${API_BASE_URL}/${c.id}/participants`;
              const resp = await fetchWithBackoff(participantsUrl, {
                method: "GET",
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${tokenStr}`,
                },
              });
              // Expecting an array of participant objects or ids
              const participants = Array.isArray(resp)
                ? resp
                : resp?.participants || [];
              // Update participants count
              setParticipantsCount((prev) => ({
                ...prev,
                [c.id]: participants.length,
              }));
              const isUserIn = participants.some((p) => {
                if (p == null) return false;
                if (typeof p === "number") return p === userId;
                if (typeof p === "string") return String(p) === String(userId);
                return String(p.id ?? p.userId ?? p) === String(userId);
              });
              return [c.id, isUserIn ? "JOINED" : "NOT_JOINED"];
            } catch (_) {
              // On error, default to NOT_JOINED to avoid blocking UI
              return [c.id, "NOT_JOINED"];
            }
          })
        );
        if (userId) {
          setUserChallengeStatus(Object.fromEntries(statusEntries));
        }
      }

      // In a real application, you would also fetch the user's current join status here
      // For this isolated example, we just trust the state set during join/leave actions.

      notification.success({
        message: "Challenges Loaded",
        description: `Fetched ${
          Array.isArray(data.content) ? data.content.length : 0
        } challenges.`,
        placement: "topRight",
      });
    } catch (error) {
      console.error("Failed to fetch challenges:", error);
      notification.error({
        message: "Data Fetch Failed",
        description: "Could not load challenges from the API endpoint.",
        placement: "topRight",
      });
      setChallenges([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchChallenges(1, pageSize);
  }, []);

  const handleCreateChallenge = async (values) => {
    setIsCreating(true);

    const payload = {
      ...values,
      startDate: values.startDate.toISOString(),
      endDate: values.endDate.toISOString(),
      entryFee: values.isPaid ? values.entryFee : 0,
      category: "Community",
      difficulty: "Medium",
      // Calculate duration in days
      duration: dayjs(values.endDate).diff(values.startDate, "day") + " Days",
      rewards: ["Bragging Rights", "New Apron"],
      leaderboard: [],
      tags: ["Dessert", "Baking"],
      participants: 0,
      progress: 0,
    };

    try {
      const response = await fetchWithBackoff(API_BASE_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
        body: JSON.stringify(payload),
      });

      // Response.ok check is handled in fetchWithBackoff, it only returns a value if ok
      response.success({
        message: "Challenge Created!",
        description: `Successfully created the challenge: "${payload.name}". Refreshing list...`,
        placement: "topRight",
      });
      await fetchChallenges();
      form.resetFields();
    } catch (error) {
      console.error("API Error (POST challenge):", error);
      notification.error({
        message: "Creation Failed",
        description:
          "Failed to submit challenge (Check console). The list refresh only occurs on a successful API response.",
        placement: "topRight",
      });
    } finally {
      setIsCreating(false);
      setIsModalVisible(false);
      fetchChallenges(currentPage, pageSize);
    }
  };

  const handleSubmitChallenge = () => {
    form
      .validateFields()
      .then((values) => {
        handleCreateChallenge(values);
      })
      .catch((info) => {
        console.log("Validate Failed:", info);
        message.error("Please complete all required fields correctly.");
      });
  };

  // Open the Submit Recipe modal from child components
  const openSubmitRecipeModal = (challengeId) => {
    setSubmitRecipeChallengeId(challengeId);
    setIsRecipeModalVisible(true);
    if (recipeForm) {
      recipeForm.resetFields();
    }
  };

  // Utility to show payment failure modal
  const showPaymentFailureModal = (details) => {
    Modal.error({
      title: "Payment Failure",
      content: (
        <>
          <p>
            We were unable to confirm your payment. You cannot join this
            challenge because of payment failure.
          </p>
          <p
            style={{
              marginTop: "10px",
              fontSize: "0.8em",
              color: token.colorError,
            }}
          >
            Details: {details || "No specific error details provided."}
          </p>
        </>
      ),
      maskClosable: true,
      okText: "OK",
    });
  };

  // Refactored function to handle Join or Leave logic for Free/Paid challenges
  const handleJoinOrLeaveChallenge = async (
    challengeId,
    isPaid,
    isUserJoined
  ) => {
    if (!user?.isAuthenticated) {
      message.error("Please log in to join or leave challenges.");
      return;
    }

    setJoiningChallengeId(challengeId);
    const token = localStorage.getItem("token"); // Get the current auth token

    const userPayload = { userId: userId };

    console.log("payload", userPayload);

    try {
      if (isUserJoined) {
        // --- LEAVE CHALLENGE logic: http://localhost:8089/api/v1/challenges/{challengeId}/leave
        const leaveUrl = `${API_BASE_URL}/${challengeId}/leave`;
        const resp = await fetchWithBackoff(leaveUrl, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(userPayload),
        });

        // Show success and flip local state immediately
        notification.success({
          message: "Challenge Left",
          placement: "topRight",
        });
        // Update local state to NOT_JOINED
        setUserChallengeStatus((prev) => ({
          ...prev,
          [challengeId]: "NOT_JOINED",
        }));
        // Refresh challenge data to update participant count
        fetchChallenges(currentPage, pageSize);
      } else {
        // --- JOIN / VERIFY PAYMENT logic ---

        // If payment already pending for this challenge, verify payment instead of starting a new one
        if (paymentPending[challengeId]) {
          const confirmPaymentUrl = `${API_BASE_URL}/${challengeId}/confirm-payment`;
          const verifyPayload = {
            userId: userId,
            paymentId: paymentPending[challengeId],
          };
          try {
            const confirmResp = await fetchWithBackoff(confirmPaymentUrl, {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
              },
              body: JSON.stringify(verifyPayload),
            });
            if (confirmResp && confirmResp.success) {
              notification.success({
                message: "Payment Verified",
                description: "You have joined the challenge.",
                placement: "topRight",
              });
              setUserChallengeStatus((prev) => ({
                ...prev,
                [challengeId]: "JOINED",
              }));
              // Clear pending payment
              setPaymentPending((prev) => {
                const next = { ...prev };
                delete next[challengeId];
                return next;
              });
              // Refresh data
              fetchChallenges(currentPage, pageSize);
            } else {
              // Show pop-up modal with error and revert button to Enroll
              const errMsg = "Payment not completed. PLease try again.";
              // Toast notification to ensure visibility
              notification.error({
                message: "Payment not verified or not completed.",
                description: errMsg,
                placement: "topRight",
              });
              // Open local modal to guarantee UI rendering
              setPaymentErrorModal({ open: true, message: errMsg });
              // Clear pending payment to switch button back to Enroll
              setPaymentPending((prev) => {
                const next = { ...prev };
                delete next[challengeId];
                return next;
              });
            }
            return;
          } catch (paymentError) {
            console.error("Payment Verification Error:", paymentError);
            notification.error({
              message: "Payment not verified or not completed.",
              description: paymentError.message || "Please try again.",
              placement: "topRight",
            });
            setPaymentErrorModal({
              open: true,
              message: "Payment not completed. Please try again.",
            });
            // Clear pending payment to switch button back to Enroll on error
            setPaymentPending((prev) => {
              const next = { ...prev };
              delete next[challengeId];
              return next;
            });
            return;
          }
        }

        // Always hit JOIN API (free or paid) to initiate join/payment session
        const joinUrl = `${API_BASE_URL}/${challengeId}/join`;
        const joinResp = await fetchWithBackoff(joinUrl, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(userPayload),
        });
        // Handle structured API response
        if (joinResp && typeof joinResp === "object") {
          // Paid initiation flow: requiresPayment true -> store paymentId and redirect
          if (joinResp.requiresPayment && isPaid && joinResp.success) {
            const { paymentId, paymentLink } = joinResp;
            if (paymentId && paymentLink) {
              setPaymentPending((prev) => ({
                ...prev,
                [challengeId]: paymentId,
              }));
              notification.info({
                message: "Payment Required",
                description: "Redirecting to payment to complete enrollment.",
                placement: "topRight",
              });
              // Open payment in a new tab for better UX
              window.open(paymentLink, "_blank", "noopener,noreferrer");
              // Do not set JOINED yet, wait for verification
            } else {
              notification.error({
                message: "Payment Initiation Failed",
                description: "Missing payment details from server.",
                placement: "topRight",
              });
            }
          } else if (
            joinResp.success === false &&
            /already joined/i.test(joinResp.error || "")
          ) {
            // Treat as joined
            notification.info({
              message: "Already Joined",
              description: "You are already participating in this challenge.",
              placement: "topRight",
            });
            setUserChallengeStatus((prev) => ({
              ...prev,
              [challengeId]: "JOINED",
            }));
          } else {
            // Free challenge (or paid with immediate success without requiresPayment)
            notification.success({
              message: "Challenge Joined",
              description: "You have joined the challenge.",
              placement: "topRight",
            });
            setUserChallengeStatus((prev) => ({
              ...prev,
              [challengeId]: "JOINED",
            }));
            // Refresh data
            fetchChallenges(currentPage, pageSize);
          }
        } else {
          // Non-JSON success fallback
          notification.success({
            message: "Challenge Joined",
            description: "You have joined the challenge.",
            placement: "topRight",
          });
          setUserChallengeStatus((prev) => ({
            ...prev,
            [challengeId]: "JOINED",
          }));
        }

        // For paid flow with payment required, do not mark joined or refresh until verification
        if (!(joinResp && joinResp.requiresPayment && isPaid)) {
          // Refresh challenge data to update participant count and progress display
          fetchChallenges(currentPage, pageSize);
        }
      }
    } catch (e) {
      console.error("Challenge Action Failed:", e.message);

      // Generic failure notification
      notification.error({
        message: isUserJoined ? "Leave Failed" : "Join Failed",
        description: e.message,
        placement: "topRight",
      });
    } finally {
      setJoiningChallengeId(null);
    }
  };

  const [allRecipes, setAllRecipes] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPagesRecipes, setTotalPagesRecipes] = useState(1);

  const fetchAllRecipes = async (pageNumber = 1) => {
    try {
      const url = `http://localhost:8089/api/v1/recipes?userId=${userId}&page=${pageNumber}&size=10&sortBy=id&direction=asc`;
      const response = await fetch(url, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
        },
      });
      if (response.status === 401 || response.status === 403) {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
        window.location.reload();
      }
      if (!response.ok) {
        throw new Error("Failed to fetch recipes");
      }
      const data = await response.json();
      console.log("Fetched Recipes:", data.content);

      // Append new recipes, avoiding duplicates
      setAllRecipes((prev) => {
        if (pageNumber === 1) return data.content;

        // Filter out duplicates based on recipe.id
        const existingIds = new Set(prev.map((r) => r.id));
        const newRecipes = data.content.filter((r) => !existingIds.has(r.id));
        return [...prev, ...newRecipes];
      });

      setPage(data.page);
      console.log("Current Page after fetch:", data.totalPages);
      setTotalPagesRecipes(data.totalPages);
    } catch (error) {
      console.error("Error fetching recipes:", error);
    }
  };

  useEffect(() => {
    if (isRecipeModalVisible) {
      fetchAllRecipes(1);
    }
  }, [isRecipeModalVisible]);

  // Add handleLoadMore function
  const handleLoadMore = () => {
    if (page < totalPagesRecipes) {
      fetchAllRecipes(page + 1);
    }
  };

  return (
    <Layout
      style={{ minHeight: "100vh", backgroundColor: token.colorBgLayout }}
    >
      <LoggedInNavbar activeKey="challenges" />
      <Header
        style={{
          background: token.colorBgContainer,
          borderBottom: `1px solid ${token.colorBorderSecondary}`,
          height: 70,
          padding: "0 24px",
        }}
      >
        <div
          style={{
            maxWidth: "1200px",
            margin: "0 auto",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            height: "100%",
          }}
        >
          <Title
            level={3}
            style={{ margin: 0, color: token.colorPrimary, fontWeight: 700 }}
          >
            Culinary Challenge Hub
          </Title>
          <Button
            type="primary"
            size="large"
            icon={<Plus size={18} style={{ marginBottom: "-3px" }} />}
            onClick={() => {
              setIsModalVisible(true);
              form.resetFields();
            }}
          >
            Create Challenge
          </Button>
        </div>
      </Header>

      <Content style={{ padding: "0 24px" }}>
        <div style={{ maxWidth: "1200px", margin: "0 auto" }}>
          {isLoading ? (
            <div style={{ textAlign: "center", padding: "100px 0" }}>
              <Spin size="large" tip="Loading Challenges..." />
            </div>
          ) : activeChallengeId ? (
            <ChallengeDetailPage
              challenge={activeChallenge}
              onBackToList={handleBackToList}
              token={token}
              handleJoinOrLeaveChallenge={handleJoinOrLeaveChallenge}
              joiningChallengeId={joiningChallengeId}
              userChallengeStatus={userChallengeStatus}
              onAddRecipe={openSubmitRecipeModal}
              participantsCount={participantsCount}
            />
          ) : (
            <ChallengeListPage
              challenges={challenges}
              totalChallenges={totalElements}
              currentPage={currentPage}
              fetchChallenges={fetchChallenges}
              onViewDetails={handleViewDetails}
              token={token}
              handleJoinOrLeaveChallenge={handleJoinOrLeaveChallenge}
              joiningChallengeId={joiningChallengeId}
              userChallengeStatus={userChallengeStatus}
              paymentPending={paymentPending}
              onAddRecipe={openSubmitRecipeModal}
              participantsCount={participantsCount}
            />
          )}
        </div>
      </Content>

      <Modal
        title={
          <Title level={3} style={{ margin: 0 }}>
            Create a New Challenge
          </Title>
        }
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={[
          <Button key="back" onClick={() => setIsModalVisible(false)}>
            Cancel
          </Button>,
          <Button
            key="submit"
            type="primary"
            loading={isCreating}
            onClick={handleSubmitChallenge}
          >
            Create Challenge
          </Button>,
        ]}
        destroyOnClose={true}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={initialValues}
          name="challenge_creation_form"
          style={{ marginTop: "20px" }}
        >
          <Form.Item
            name="name"
            label="Challenge Name"
            rules={[
              { required: true, message: "Please input the challenge name!" },
            ]}
          >
            <Input placeholder="e.g., Fresh Pasta Masterclass" />
          </Form.Item>

          <Form.Item
            name="description"
            label="Description"
            rules={[
              { required: true, message: "Please describe the challenge!" },
            ]}
          >
            <Input.TextArea
              rows={4}
              placeholder="Describe the goal and rules of the challenge."
            />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="startDate"
                label="Start Date"
                rules={[
                  { required: true, message: "Please select a start date!" },
                ]}
              >
                <DatePicker
                  showTime
                  format="YYYY-MM-DD HH:mm"
                  style={{ width: "100%" }}
                  disabledDate={disabledDate}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="endDate"
                label="End Date"
                rules={[
                  { required: true, message: "Please select an end date!" },
                ]}
              >
                <DatePicker
                  showTime
                  format="YYYY-MM-DD HH:mm"
                  style={{ width: "100%" }}
                  disabledDate={disabledDate}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="type"
            label="Winning Criteria (Type)"
            rules={[
              {
                required: true,
                message: "Please select the winning criteria!",
              },
            ]}
          >
            <Select placeholder="Select criteria">
              <Option value="MOST_RECIPES">Most Recipes Submitted</Option>
              <Option value="HIGHEST_SCORE">Highest Average Score</Option>
              <Option value="LEAST_INGREDIENTS">
                Least Ingredients Used (Minimalism)
              </Option>
            </Select>
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="isPaid" label="Paid Challenge?">
                <Select placeholder="Select paid status">
                  <Option value={true}>Yes (Paid)</Option>
                  <Option value={false}>No (Free)</Option>
                </Select>
              </Form.Item>
            </Col>
            {/* Entry Fee is only shown if isPaid is true */}
            <Col span={12}>
              <Form.Item
                noStyle
                shouldUpdate={(prevValues, currentValues) =>
                  prevValues.isPaid !== currentValues.isPaid
                }
              >
                {() =>
                  isPaid && (
                    <Form.Item
                      name="entryFee"
                      label="Entry Fee (₹)"
                      rules={[
                        {
                          required: isPaid,
                          message: "Fee is required for paid challenges!",
                        },
                        {
                          type: "number",
                          min: 0.01,
                          message: "Fee must be greater than 0.",
                        },
                      ]}
                    >
                      <InputNumber
                        min={0}
                        step={0.01}
                        formatter={(value) =>
                          `₹ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                        }
                        parser={(value) => value.replace(/₹\s?|(,*)/g, "")}
                        style={{ width: "100%" }}
                      />
                    </Form.Item>
                  )
                }
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>

      {/* Submit Recipe Modal */}
      <Modal
        title={
          <Title level={3} style={{ margin: 0 }}>
            Submit Recipe to Challenge
          </Title>
        }
        open={isRecipeModalVisible}
        onCancel={() => {
          setIsRecipeModalVisible(false);
          recipeForm.resetFields();
          setAllRecipes([]); // Clear recipes on close
          setPage(1);
        }}
        footer={[
          <Button
            key="cancel"
            onClick={() => {
              setIsRecipeModalVisible(false);
              recipeForm.resetFields();
              setAllRecipes([]);
              setPage(1);
            }}
          >
            Cancel
          </Button>,
          <Button
            key="submit"
            type="primary"
            onClick={async () => {
              try {
                const values = await recipeForm.validateFields();
                if (!submitRecipeChallengeId) {
                  message.error("No challenge selected.");
                  return;
                }
                const submitUrl = `${API_BASE_URL}/${submitRecipeChallengeId}/submit-recipe`;
                const body = {
                  recipeId: Number(values.recipeId),
                  userId: userId,
                  userName: user?.userData?.username || "Unknown User", // Use actual username
                };

                console.log("Submitting payload:", body);

                await fetchWithBackoff(submitUrl, {
                  method: "POST",
                  headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${localStorage.getItem("token")}`,
                  },
                  body: JSON.stringify(body),
                });
                notification.success({
                  message: "Recipe Submitted",
                  description:
                    "Your recipe has been submitted to the challenge.",
                  placement: "topRight",
                });
                setIsRecipeModalVisible(false);
                recipeForm.resetFields();
                setAllRecipes([]);
                setPage(1);
                fetchChallenges(currentPage, pageSize);
              } catch (err) {
                console.error("Submit error:", err);
                notification.error({
                  message: "Submit Failed",
                  description: err.message,
                  placement: "topRight",
                });
              }
            }}
          >
            Submit Recipe
          </Button>,
        ]}
        destroyOnClose
      >
        <Form form={recipeForm} layout="vertical" name="submit_recipe_form">
          <Form.Item
            name="recipeId"
            label="Select Recipe"
            rules={[{ required: true, message: "Please select a recipe!" }]}
          >
            <Select
              showSearch
              placeholder="Select a recipe"
              optionFilterProp="children"
              filterOption={(input, option) =>
                option.children.toLowerCase().includes(input.toLowerCase())
              }
              style={{ width: "100%" }}
              dropdownRender={(menu) => (
                <>
                  {menu}
                  {page < totalPagesRecipes && (
                    <div
                      style={{
                        textAlign: "center",
                        padding: "8px",
                        borderTop: "1px solid #f0f0f0",
                      }}
                    >
                      <Button
                        type="link"
                        onClick={handleLoadMore}
                        style={{ width: "100%" }}
                      >
                        Load More Recipes
                      </Button>
                    </div>
                  )}
                </>
              )}
            >
              {allRecipes.map((recipe) => (
                <Select.Option key={recipe.id} value={recipe.id}>
                  {recipe?.title} (By: {recipe?.username})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
      {/* Payment Error Modal (UI-driven) */}
      <Modal
        title={
          <Title level={4} style={{ margin: 0 }}>
            Payment not verified or not completed.
          </Title>
        }
        open={paymentErrorModal.open}
        onCancel={() => setPaymentErrorModal({ open: false, message: "" })}
        footer={[
          <Button
            key="ok"
            type="primary"
            onClick={() => setPaymentErrorModal({ open: false, message: "" })}
          >
            OK
          </Button>,
        ]}
        destroyOnClose
      >
        <Paragraph style={{ marginBottom: 0 }}>
          {paymentErrorModal.message || "Please try again."}
        </Paragraph>
      </Modal>
    </Layout>
  );
};

export default Challenge;
