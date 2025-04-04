import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
  Alert,
  Paper,
  Chip,
} from '@mui/material';
import { PieChart, Pie, Tooltip, Legend, ResponsiveContainer, Cell } from 'recharts';

const API_BASE_URL = 'http://localhost:8081/api';

const CrawlerDashboard = () => {
  const [crawlerStatus, setCrawlerStatus] = useState({
    running: false,
    lastRun: null,
    totalProcessed: 0,
    newReviews: [],
    crawlingSpeed: 0,
    currentTopic: '',
    errorLog: '',
    processedTopics: '',
    successfulReviews: 0,
    failedReviews: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isToggling, setIsToggling] = useState(false);
  const [previousReviews, setPreviousReviews] = useState([]);

  const COLORS = ['#00C49F', '#FF8042'];

  const fetchCrawlerStatus = useCallback(async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/crawler/status`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      
      // Store current reviews before updating
      setPreviousReviews(crawlerStatus.newReviews || []);
      
      setCrawlerStatus(prevStatus => ({
        ...prevStatus,
        ...data,
        running: data.running,
        newReviews: data.newReviews || [],
      }));
      setError(null);
    } catch (err) {
      console.error('Error fetching crawler status:', err);
      setError('Failed to fetch crawler status. Please try again later.');
    } finally {
      setLoading(false);
    }
  }, []);

  const toggleCrawler = async () => {
    if (isToggling) return;
    
    setIsToggling(true);
    try {
      const action = crawlerStatus.running ? 'stop' : 'start';
      console.log(`Attempting to ${action} crawler`);
      
      const toggleResponse = await fetch(`${API_BASE_URL}/crawler/${action}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!toggleResponse.ok) {
        throw new Error(`Failed to ${action} crawler: ${toggleResponse.status}`);
      }

      const statusData = await toggleResponse.json();
      console.log('Received status after toggle:', statusData);
      
      setCrawlerStatus(prevStatus => ({
        ...prevStatus,
        ...statusData,
        running: statusData.running,
      }));

      if (statusData.running) {
        fetchCrawlerStatus();
      }
      
      setError(null);
    } catch (err) {
      console.error('Error toggling crawler:', err);
      setError(`Failed to ${crawlerStatus.running ? 'stop' : 'start'} the crawler. Please try again later.`);
    } finally {
      setIsToggling(false);
    }
  };

  useEffect(() => {
    fetchCrawlerStatus();
    const interval = setInterval(fetchCrawlerStatus, 5000);
    return () => clearInterval(interval);
  }, [fetchCrawlerStatus]);

  const renderErrorLog = () => {
    if (!crawlerStatus.errorLog) return null;
    return (
      <Card sx={{ mt: 2, backgroundColor: '#fff3f3' }}>
        <CardContent>
          <Typography variant="h6" color="error">Error Log</Typography>
          <List dense>
            {crawlerStatus.errorLog.split('\n').map((error, index) => (
              <ListItem key={index}>
                <ListItemText primary={error} />
              </ListItem>
            ))}
          </List>
        </CardContent>
      </Card>
    );
  };

  const renderProgressStats = () => {
    const pieData = [
      { name: 'Successful', value: crawlerStatus.successfulReviews },
      { name: 'Failed', value: crawlerStatus.failedReviews },
    ];

    return (
      <Grid container spacing={2} sx={{ mt: 2 }}>
        <Grid item xs={12}>
          <Paper 
            elevation={3} 
            sx={{ 
              p: 2,
              background: 'linear-gradient(to right bottom, #ffffff, #fafafa)',
            }}
          >
            <Grid container spacing={2} alignItems="center">
              {/* Status Info */}
              <Grid item xs={12} md={3}>
                <Box sx={{ 
                  p: 1.5, 
                  bgcolor: crawlerStatus.running ? '#d5f4d6' : 'grey.100',
                  borderRadius: 1,
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 0.5
                }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 500 }}>
                    Status: <span style={{ color: crawlerStatus.running ? '#2e7d32' : '#666' }}>
                      {crawlerStatus.running ? 'Running' : 'Stopped'}
                    </span>
                  </Typography>
                  <Typography variant="subtitle2" noWrap>
                    Topic: <strong>{crawlerStatus.currentTopic || 'None'}</strong>
                  </Typography>
                  {crawlerStatus.running && crawlerStatus.currentReview && (
                    <Typography variant="subtitle2" noWrap title={crawlerStatus.currentReview}>
                      Review: <strong>{crawlerStatus.currentReview}</strong>
                    </Typography>
                  )}
                </Box>
              </Grid>

              {/* Quick Stats */}
              <Grid item xs={12} md={6}>
                <Grid container spacing={1}>
                  <Grid item xs={3}>
                    <Box sx={{ textAlign: 'center' }}>
                      <Typography variant="h6" sx={{ color: 'primary.main', fontWeight: 'bold', lineHeight: 1 }}>
                        {crawlerStatus.totalProcessed}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        Total
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={3}>
                    <Box sx={{ textAlign: 'center' }}>
                      <Typography variant="h6" sx={{ color: 'secondary.main', fontWeight: 'bold', lineHeight: 1 }}>
                        {crawlerStatus.crawlingSpeed.toFixed(1)}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        Rev/min
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={3}>
                    <Box sx={{ textAlign: 'center' }}>
                      <Typography variant="h6" sx={{ color: 'success.main', fontWeight: 'bold', lineHeight: 1 }}>
                        {crawlerStatus.successfulReviews}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        Success
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={3}>
                    <Box sx={{ textAlign: 'center' }}>
                      <Typography variant="h6" sx={{ color: 'error.main', fontWeight: 'bold', lineHeight: 1 }}>
                        {crawlerStatus.failedReviews}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                        Failed
                      </Typography>
                    </Box>
                  </Grid>
                </Grid>
              </Grid>

              {/* Mini Chart */}
              <Grid item xs={12} md={3}>
                <Box sx={{ height: '80px' }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={[
                          { name: 'Successful', value: crawlerStatus.successfulReviews },
                          { name: 'Failed', value: crawlerStatus.failedReviews },
                        ]}
                        cx="50%"
                        cy="50%"
                        innerRadius="60%"
                        outerRadius="90%"
                        fill="#8884d8"
                        dataKey="value"
                        label={false}
                      >
                        {[
                          { name: 'Successful', value: crawlerStatus.successfulReviews },
                          { name: 'Failed', value: crawlerStatus.failedReviews },
                        ].map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                    </PieChart>
                  </ResponsiveContainer>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>

        {/* New Reviews Section */}
        {crawlerStatus.newReviews && crawlerStatus.newReviews.length > 0 && (
          <Grid item xs={12}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Newly Processed Reviews ({crawlerStatus.newReviews.length})
              </Typography>
              {crawlerStatus.lastRun && (
                <Typography variant="body2" color="textSecondary">
                  Last Update: {new Date(crawlerStatus.lastRun).toLocaleString()}
                </Typography>
              )}
            </Box>
            <Grid container spacing={2}>
              {[...crawlerStatus.newReviews].reverse().map((review, index) => (
                <Grid item xs={12} sm={6} md={4} lg={3} key={index}>
                  <Card 
                    elevation={2}
                    sx={{
                      height: '100%',
                      display: 'flex',
                      flexDirection: 'column',
                      transition: 'all 0.3s ease-in-out',
                      ...(index === 0 && {
                        animation: 'highlightNew 2s ease-out',
                        '@keyframes highlightNew': {
                          '0%': {
                            backgroundColor: 'success.light',
                            transform: 'translateY(0)',
                          },
                          '100%': {
                            backgroundColor: 'background.paper',
                            transform: 'translateY(0)',
                          }
                        },
                      }),
                      '&:hover': {
                        transform: 'translateY(-4px)',
                        boxShadow: 4,
                      },
                    }}
                  >
                    <CardContent>
                      <Typography variant="h6" gutterBottom noWrap title={review.title}>
                        {review.title}
                      </Typography>
                      <Typography color="textSecondary" gutterBottom>
                        Topic: {review.topic}
                      </Typography>
                      {review.authors && (
                        <Typography variant="body2" color="textSecondary" sx={{ mb: 1 }}>
                          Authors: {review.authors}
                        </Typography>
                      )}
                      {review.publicationDate && (
                        <Typography variant="body2" color="textSecondary">
                          Published: {new Date(review.publicationDate).toLocaleDateString()}
                        </Typography>
                      )}
                      <Box sx={{ mt: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography 
                          variant="body2" 
                          sx={{ 
                            color: review.crawlStatus === 'COMPLETED' ? 'success.main' : 'error.main',
                            fontWeight: 'medium',
                          }}
                        >
                          Status: {review.crawlStatus}
                        </Typography>
                      </Box>
                    </CardContent>
                    <CardContent sx={{ mt: 'auto', pt: 1 }}>
                      <Button 
                        size="small" 
                        href={review.url} 
                        target="_blank"
                        rel="noopener noreferrer"
                        sx={{ textTransform: 'none' }}
                      >
                        View Review
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Grid>
        )}
      </Grid>
    );
  };

  const isNewReview = (review) => {
    return !previousReviews.some(prevReview => prevReview.title === review.title);
  };

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                  <Typography variant="h5" component="div">
                    Crawler Dashboard
                  </Typography>
                  <Typography color="textSecondary">
                    {crawlerStatus.running ? 'Running' : 'Stopped'}
                  </Typography>
                </Grid>
                <Grid item>
                  <Button
                    variant="contained"
                    color={crawlerStatus.running ? 'secondary' : 'primary'}
                    onClick={toggleCrawler}
                    disabled={loading || isToggling}
                  >
                    {isToggling ? (
                      <CircularProgress size={24} color="inherit" />
                    ) : (
                      crawlerStatus.running ? 'Stop Crawler' : 'Start Crawler'
                    )}
                  </Button>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {loading && (
        <Box display="flex" justifyContent="center" mt={3}>
          <CircularProgress />
        </Box>
      )}

      {error && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {error}
        </Alert>
      )}

      {!loading && !error && (
        <Grid container spacing={2} sx={{ mt: 2 }}>
          {/* Progress Stats */}
          <Grid item xs={12}>
            <Paper 
              elevation={3} 
              sx={{ 
                p: 2,
                background: 'linear-gradient(to right bottom, #ffffff, #fafafa)',
              }}
            >
              <Grid container spacing={2} alignItems="center">
                {/* Status Info */}
                <Grid item xs={12} md={3}>
                  <Box sx={{ 
                    p: 1.5, 
                    bgcolor: crawlerStatus.running ? 'success.light' : 'grey.100',
                    borderRadius: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 0.5
                  }}>
                    <Typography variant="subtitle2" sx={{ fontWeight: 500 }}>
                      Status: <span style={{ color: crawlerStatus.running ? '#2e7d32' : '#666' }}>
                        {crawlerStatus.running ? 'Running' : 'Stopped'}
                      </span>
                    </Typography>
                    <Typography variant="subtitle2" noWrap>
                      Topic: <strong>{crawlerStatus.currentTopic || 'None'}</strong>
                    </Typography>
                    {crawlerStatus.running && crawlerStatus.currentReview && (
                      <Typography variant="subtitle2" noWrap title={crawlerStatus.currentReview}>
                        Review: <strong>{crawlerStatus.currentReview}</strong>
                      </Typography>
                    )}
                  </Box>
                </Grid>

                {/* Quick Stats */}
                <Grid item xs={12} md={6}>
                  <Grid container spacing={1}>
                    <Grid item xs={3}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="h6" sx={{ color: 'primary.main', fontWeight: 'bold', lineHeight: 1 }}>
                          {crawlerStatus.totalProcessed}
                        </Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                          Total
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="h6" sx={{ color: 'secondary.main', fontWeight: 'bold', lineHeight: 1 }}>
                          {crawlerStatus.crawlingSpeed.toFixed(1)}
                        </Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                          Rev/min
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="h6" sx={{ color: 'success.main', fontWeight: 'bold', lineHeight: 1 }}>
                          {crawlerStatus.successfulReviews}
                        </Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                          Success
                        </Typography>
                      </Box>
                    </Grid>
                    <Grid item xs={3}>
                      <Box sx={{ textAlign: 'center' }}>
                        <Typography variant="h6" sx={{ color: 'error.main', fontWeight: 'bold', lineHeight: 1 }}>
                          {crawlerStatus.failedReviews}
                        </Typography>
                        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                          Failed
                        </Typography>
                      </Box>
                    </Grid>
                  </Grid>
                </Grid>

                {/* Mini Chart */}
                <Grid item xs={12} md={3}>
                  <Box sx={{ height: '80px' }}>
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={[
                            { name: 'Successful', value: crawlerStatus.successfulReviews },
                            { name: 'Failed', value: crawlerStatus.failedReviews },
                          ]}
                          cx="50%"
                          cy="50%"
                          innerRadius="60%"
                          outerRadius="90%"
                          fill="#8884d8"
                          dataKey="value"
                          label={false}
                        >
                          {[
                            { name: 'Successful', value: crawlerStatus.successfulReviews },
                            { name: 'Failed', value: crawlerStatus.failedReviews },
                          ].map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                          ))}
                        </Pie>
                      </PieChart>
                    </ResponsiveContainer>
                  </Box>
                </Grid>
              </Grid>
            </Paper>
          </Grid>

          {/* New Reviews Section */}
          {crawlerStatus.newReviews && crawlerStatus.newReviews.length > 0 && (
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Newly Processed Reviews ({crawlerStatus.newReviews.length})
                </Typography>
                {crawlerStatus.lastRun && (
                  <Typography variant="body2" color="textSecondary">
                    Last Update: {new Date(crawlerStatus.lastRun).toLocaleString()}
                  </Typography>
                )}
              </Box>
              <Grid container spacing={2}>
                {[...crawlerStatus.newReviews].reverse().map((review, index) => (
                  <Grid item xs={12} sm={6} md={4} lg={3} key={review.title}>
                    <Card 
                      elevation={2}
                      sx={{
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        transition: 'all 0.3s ease-in-out',
                        ...(isNewReview(review) && {
                          animation: 'highlightNew 2s ease-out',
                          '@keyframes highlightNew': {
                            '0%': {
                              backgroundColor: 'success.light',
                              transform: 'translateY(-4px)',
                            },
                            '100%': {
                              backgroundColor: 'background.paper',
                              transform: 'translateY(0)',
                            }
                          },
                        }),
                        '&:hover': {
                          transform: 'translateY(-4px)',
                          boxShadow: 4,
                        },
                      }}
                    >
                      <CardContent>
                        <Typography variant="h6" gutterBottom noWrap title={review.title}>
                          {review.title}
                        </Typography>
                        <Typography color="textSecondary" gutterBottom>
                          Topic: {review.topic}
                        </Typography>
                        {review.authors && (
                          <Typography variant="body2" color="textSecondary" sx={{ mb: 1 }}>
                            Authors: {review.authors}
                          </Typography>
                        )}
                        {review.publicationDate && (
                          <Typography variant="body2" color="textSecondary">
                            Published: {new Date(review.publicationDate).toLocaleDateString()}
                          </Typography>
                        )}
                        <Box sx={{ mt: 1, display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography 
                            variant="body2" 
                            sx={{ 
                              color: review.crawlStatus === 'COMPLETED' ? 'success.main' : 'error.main',
                              fontWeight: 'medium',
                            }}
                          >
                            Status: {review.crawlStatus}
                          </Typography>
                        </Box>
                      </CardContent>
                      <CardContent sx={{ mt: 'auto', pt: 1 }}>
                        <Button 
                          size="small" 
                          href={review.url} 
                          target="_blank"
                          rel="noopener noreferrer"
                          sx={{ textTransform: 'none' }}
                        >
                          View Review
                        </Button>
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            </Grid>
          )}
        </Grid>
      )}

      {!loading && !error && renderErrorLog()}
    </Box>
  );
};

export default CrawlerDashboard; 