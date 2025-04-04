import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Select,
  MenuItem,
  TextField,
  Button,
  Pagination,
  CircularProgress,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
} from '@mui/material';
import Statistics from './Statistics';

const ReviewList = () => {
  const [reviews, setReviews] = useState([]);
  const [topics, setTopics] = useState([]);
  const [selectedTopic, setSelectedTopic] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState(null);
  const [selectedReview, setSelectedReview] = useState(null);

  const fetchReviews = useCallback(async () => {
    setLoading(true);
    try {
      let url = `http://localhost:8081/api/reviews?page=${page - 1}&size=10`;
      if (selectedTopic) {
        url += `&topic=${encodeURIComponent(selectedTopic)}`;
      }
      if (searchTerm) {
        url += `&search=${encodeURIComponent(searchTerm)}`;
      }

      const response = await fetch(url);
      const data = await response.json();
      setReviews(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Error fetching reviews:', error);
    } finally {
      setLoading(false);
    }
  }, [page, selectedTopic, searchTerm]);

  const fetchTopics = useCallback(async () => {
    try {
      const response = await fetch('http://localhost:8081/api/reviews/topics');
      const data = await response.json();
      setTopics(data);
    } catch (error) {
      console.error('Error fetching topics:', error);
    }
  }, []);

  const fetchStats = useCallback(async () => {
    try {
      const response = await fetch('http://localhost:8081/api/reviews/stats');
      const data = await response.json();
      setStats(data);
    } catch (error) {
      console.error('Error fetching stats:', error);
    }
  }, []);

  useEffect(() => {
    fetchTopics();
    fetchStats();
  }, [fetchTopics, fetchStats]);

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews]);

  const handleRetryFailed = async () => {
    try {
      await fetch('http://localhost:8081/api/reviews/retry-failed', { method: 'POST' });
      fetchStats();
      fetchReviews();
    } catch (error) {
      console.error('Error retrying failed reviews:', error);
    }
  };

  const handlePageChange = (event, value) => {
    setPage(value);
  };

  const handleTopicChange = (event) => {
    setSelectedTopic(event.target.value);
    setPage(1);
  };

  const handleSearch = (event) => {
    setSearchTerm(event.target.value);
    setPage(1);
  };

  const handleReviewClick = (review) => {
    setSelectedReview(review);
  };

  const handleCloseDialog = () => {
    setSelectedReview(null);
  };

  return (
    <Box>
      {/* Statistics Dashboard */}
      <Statistics stats={stats} />

      {/* Filters */}
      <Box sx={{ mb: 3, display: 'flex', gap: 2, alignItems: 'center' }}>
        <Select
          value={selectedTopic}
          onChange={handleTopicChange}
          displayEmpty
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All Topics</MenuItem>
          {topics.map((topic) => (
            <MenuItem key={topic} value={topic}>{topic}</MenuItem>
          ))}
        </Select>

        <TextField
          placeholder="Search reviews..."
          value={searchTerm}
          onChange={handleSearch}
          sx={{ flexGrow: 1 }}
        />

        <Button
          variant="contained"
          onClick={handleRetryFailed}
          disabled={!stats || stats.failedContent === 0}
        >
          Retry Failed ({stats?.failedContent || 0})
        </Button>
      </Box>

      {/* Reviews List */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <>
          <Grid container spacing={2}>
            {reviews.map((review) => (
              <Grid item xs={12} key={review.id}>
                <Card
                  sx={{
                    p: 2,
                    cursor: 'pointer',
                    '&:hover': { bgcolor: 'action.hover' }
                  }}
                  onClick={() => handleReviewClick(review)}
                >
                  <Typography variant="h6" gutterBottom>
                    {review.title}
                  </Typography>
                  <Typography color="textSecondary" gutterBottom>
                    {review.authors}
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                    <Chip label={review.topic} color="primary" size="small" />
                    <Chip
                      label={review.crawlStatus}
                      color={review.crawlStatus === 'COMPLETED' ? 'success' : review.crawlStatus === 'FAILED' ? 'error' : 'warning'}
                      size="small"
                    />
                  </Box>
                  <Typography variant="body2" color="textSecondary">
                    {review.date}
                  </Typography>
                </Card>
              </Grid>
            ))}
          </Grid>

          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
            <Pagination
              count={totalPages}
              page={page}
              onChange={handlePageChange}
              color="primary"
            />
          </Box>
        </>
      )}

      {/* Review Content Dialog */}
      <Dialog
        open={Boolean(selectedReview)}
        onClose={handleCloseDialog}
        maxWidth="md"
        fullWidth
      >
        {selectedReview && (
          <>
            <DialogTitle>{selectedReview.title}</DialogTitle>
            <DialogContent>
              <Typography variant="subtitle1" gutterBottom>
                Authors: {selectedReview.authors}
              </Typography>
              <Typography variant="subtitle2" color="textSecondary" gutterBottom>
                Published: {selectedReview.date}
              </Typography>
              <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap', mt: 2 }}>
                {selectedReview.content || 'No content available'}
              </Typography>
            </DialogContent>
          </>
        )}
      </Dialog>
    </Box>
  );
};

export default ReviewList; 