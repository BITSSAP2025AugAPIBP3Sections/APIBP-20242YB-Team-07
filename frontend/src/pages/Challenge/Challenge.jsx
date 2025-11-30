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
    if (!response.ok) {
      throw new Error(
        `HTTP error! Status: ${response.status} - ${response.statusText}`
      );
    }
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
      return await response.json();
    }
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
      <Text
        style={{
          fontSize: "12px",
          color: color,
          fontWeight: "600",
          textTransform: "uppercase",
        }}
      >
        {difficulty || "N/A"}
      </Text>
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
  joinChallenge,
  joiningChallengeId,
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
          const buttonDisabled = dp.percent === 100 || joining;
          const buttonText =
            dp.percent === 100
              ? "Challenge Completed!"
              : joining
              ? "Joining..."
              : challenge.isPaid
              ? `Enroll for ₹${challenge.entryFee?.toFixed(2)}`
              : "Join Challenge";

          <Button
            type={dp.percent === 100 ? "default" : "primary"}
            loading={joining}
            disabled={dp.percent === 100 || joining}
            onClick={(e) => {
              e.stopPropagation();
              joinChallenge(challenge.id);
            }}
          >
            {dp.percent === 100
              ? "Challenge Completed!"
              : joining
              ? "Joining..."
              : challenge.isPaid
              ? `Enroll ₹${challenge.entryFee?.toFixed(2)}`
              : "Join Challenge"}
          </Button>;
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
                        "https://placehold.co/600x400/F4A261/000000?text=Recipe+Challenge+Image"
                      }
                      style={{
                        width: "100%",
                        height: "100%",
                        objectFit: "cover",
                      }}
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src =
                          "https://placehold.co/600x400/F4A261/000000?text=Recipe+Challenge+Image";
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
                          {challenge.duration || "N/A"}
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
                            {challenge.participants || 0} chefs joined
                          </Text>
                          {getCostTag(challenge?.isPaid, challenge?.entryFee)}
                        </Space>

                        <Progress
                          percent={dp.percent}
                          size="small"
                          status={dp.percent === 100 ? "success" : "active"}
                          strokeColor={
                            getDifficultyColor(challenge.difficulty).color
                          }
                        />
                        <Text type="secondary" style={{ fontSize: 11 }}>
                          {dp.percent === 100
                            ? "Completed"
                            : `${dp.elapsedDays}d done • ${dp.remainingDays}d left (${dp.percent}%)`}
                        </Text>
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
  joinChallenge,
  joining,
  user,
}) => {
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

  const buttonText =
    challenge.progress === 100
      ? "Challenge Completed!"
      : challenge.isPaid
      ? `Enroll for ₹${challenge.entryFee.toFixed(2)}`
      : "Join Challenge";

  const totalDays = parseTotalDays(challenge.duration);
  const remainingDays = Math.max(
    0,
    totalDays * (1 - (challenge.progress || 0) / 100)
  );

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
              "https://placehold.co/900x350/F4A261/000000?text=Recipe+Challenge+View"
            }
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
            onError={(e) => {
              e.target.onerror = null;
              e.target.src =
                "https://placehold.co/900x350/F4A261/000000?text=Recipe+Challenge+View";
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
              <Paragraph
                type="secondary"
                style={{ fontSize: "1.1em", marginTop: "4px" }}
              >
                {challenge.category || "General"} Challenge
              </Paragraph>
            </Col>
            <Col>{getCostTag(challenge?.isPaid, challenge?.entryFee)}</Col>
          </Row>

          <Space
            size="large"
            style={{ marginTop: "16px", marginBottom: "24px" }}
            wrap
          >
            <Tag
              color={getDifficultyColor(challenge.difficulty).tag}
              style={{ padding: "6px 12px", fontSize: "14px" }}
            >
              <Star
                size={14}
                style={{ marginBottom: "-2px", marginRight: "4px" }}
              />{" "}
              Skill Level: {challenge.difficulty || "N/A"}
            </Tag>
            <Text strong>
              <Clock size={16} style={{ marginBottom: "-3px" }} /> Duration:{" "}
              {challenge.duration || "N/A"}
            </Text>
            <Text strong>
              <Users size={16} style={{ marginBottom: "-3px" }} />{" "}
              {(challenge.participants || 0).toLocaleString()} Chefs
            </Text>
          </Space>

          <Divider />

          <Title level={3} style={{ color: diffColor, marginTop: "24px" }}>
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
            <Text strong style={{ display: "block", marginBottom: "8px" }}>
              Days Completed: <Text type="secondary">{totalDays} Days</Text>
            </Text>
            <Row align="middle" gutter={24}>
              <Col xs={24} sm={18}>
                <Progress
                  percent={challenge.progress || 0}
                  format={() => `${Math.ceil(remainingDays)} Days Remaining`}
                  status={challenge.progress === 100 ? "success" : "active"}
                  strokeColor={diffColor}
                  showInfo
                />
              </Col>
              <Col xs={24} sm={6}>
                <Button
                  type={challenge.progress === 100 ? "default" : "primary"}
                  size="large"
                  icon={
                    challenge.progress === 100 ? (
                      <CheckCircle size={18} />
                    ) : (
                      <Rocket size={18} />
                    )
                  }
                  block
                  disabled={challenge.progress === 100}
                  onClick={() => joinChallenge(challenge.id)}
                >
                  {buttonText}
                </Button>
              </Col>
            </Row>
          </div>

          {/* Rewards */}
          <Title level={3} style={{ color: "#E76F51", marginTop: "30px" }}>
            <Trophy
              size={24}
              style={{ marginBottom: "-5px", marginRight: "8px" }}
            />{" "}
            Rewards & Recognition
          </Title>
          <Space size={[12, 12]} wrap>
            {(challenge.rewards || []).map((reward, index) => (
              <Tag
                key={index}
                color="volcano"
                style={{
                  padding: "8px 16px",
                  borderRadius: "8px",
                  fontSize: "14px",
                }}
              >
                {reward}
              </Tag>
            ))}
          </Space>

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
          <List
            itemLayout="horizontal"
            dataSource={challenge.leaderboard || []}
            style={{
              marginTop: "16px",
              backgroundColor: token.colorBgContainer,
              padding: "16px",
              borderRadius: "8px",
            }}
            renderItem={(item) => (
              <List.Item>
                <List.Item.Meta
                  avatar={
                    <Statistic
                      title="Rank"
                      value={item.rank}
                      valueStyle={{
                        color:
                          item.rank === 1
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
                      {item.rank <= 3 && (
                        <Trophy
                          size={16}
                          style={{
                            marginBottom: "-3px",
                            marginRight: "4px",
                            color:
                              item.rank === 1
                                ? "#FFD700"
                                : item.rank === 2
                                ? "#C0C0C0"
                                : "#CD7F32",
                          }}
                        />
                      )}
                      {item.name}
                    </Text>
                  }
                  description={`Completed ${item.recipes || 0} recipes`}
                />
                <div>
                  <Text strong style={{ color: diffColor }}>
                    {item.score || 0} Points
                  </Text>
                </div>
              </List.Item>
            )}
          />

          <Divider style={{ margin: "40px 0 20px 0" }} />

          {/* Tags */}
          <Text strong>Focus Areas:</Text>
          <Space size={[8, 8]} wrap style={{ marginTop: "8px" }}>
            {(challenge.tags || []).map((tag, index) => (
              <Tag key={index} color="blue" style={{ borderRadius: "4px" }}>
                {tag}
              </Tag>
            ))}
          </Space>
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
  const [joiningChallengeId, setJoiningChallengeId] = useState(null);

  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 6;

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

  const startIndex = (currentPage - 1) * pageSize;
  const endIndex = startIndex + pageSize;
  const paginatedChallenges = challenges.slice(startIndex, endIndex);

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

      setChallenges(data.content || EmptyChallengeData);
      setTotalPages(data.totalPages ?? 1);
      setTotalElements(data.totalElements ?? data.content?.length ?? 0);
      setCurrentPage((data.number ?? page - 1) + 1);

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

      if (response.ok) {
        notification.success({
          message: "Challenge Created!",
          description: `Successfully created the challenge: "${payload.name}". Refreshing list...`,
          placement: "topRight",
        });
        await fetchChallenges();

        form.resetFields();
      } else {
        throw new Error(`Server responded with status: ${response.status}`);
      }
    } catch (error) {
      console.warn("API Error (Simulating POST failure):", error);
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

  const joinChallenge = async (challengeId) => {
    if (!user?.isAuthenticated) {
      message.error("Please log in to join challenges.");
      return;
    }
    setJoiningChallengeId(challengeId);
    try {
      const resp = await fetch(`${API_BASE_URL}/${challengeId}/join`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${localStorage.getItem("token")}`,
          "X-User-Id": user?.userData?.id,
        },
        body: JSON.stringify({ userId: user?.userData?.id }),
      });
      if (!resp.ok) throw new Error((await resp.text()) || "Join failed");
      notification.success({
        message: "Joined Challenge",
        placement: "topRight",
      });
      await fetchChallenges(currentPage, pageSize);
    } catch (e) {
      notification.error({
        message: "Join Failed",
        description: e.message,
        placement: "topRight",
      });
    } finally {
      setJoiningChallengeId(null);
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
              joinChallenge={joinChallenge}
              joiningChallengeId={joiningChallengeId}
            />
          ) : (
            <ChallengeListPage
              challenges={challenges}
              totalChallenges={totalElements}
              currentPage={currentPage}
              fetchChallenges={fetchChallenges}
              onViewDetails={handleViewDetails}
              token={token}
              joinChallenge={joinChallenge}
              joiningChallengeId={joiningChallengeId}
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
          // ⭐️ FIX: Use handleSubmitChallenge for validation before calling API
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
                      label="Entry Fee ($)"
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
                          `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")
                        }
                        parser={(value) => value.replace(/\$\s?|(,*)/g, "")}
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
    </Layout>
  );
};

export default Challenge;
