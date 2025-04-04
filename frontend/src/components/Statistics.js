import React from 'react';
import { Box, Card, CardContent, Typography, Grid } from '@mui/material';
import { PieChart } from '@mui/x-charts';
import { ResponsiveContainer, BarChart, XAxis, YAxis, CartesianGrid, Tooltip, Legend, Bar } from 'recharts';

const Statistics = ({ stats }) => {
  if (!stats || !stats.reviewsByTopic) return null;

  // Prepare data for topic distribution chart
  const topicData = Object.entries(stats.reviewsByTopic || {}).map(([topic, count]) => ({
    topic,
    count: Number(count)
  }));

  // Sort topics by count
  topicData.sort((a, b) => b.count - a.count);

  // Prepare data for pie chart
  const statusData = [
    { label: 'Completed', value: stats.totalReviews - stats.failedContent - stats.pendingContent },
    { label: 'Failed', value: stats.failedContent },
    { label: 'Pending', value: stats.pendingContent }
  ].filter(item => item.value > 0);

  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h5" gutterBottom>
        Statistics Dashboard
      </Typography>
      <Grid container spacing={3}>
        {/* Topic Distribution Chart */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Reviews by Topic
              </Typography>
              <Box sx={{ height: 300, width: '100%' }}>
                {topicData.length > 0 && (
                  <ResponsiveContainer>
                    <BarChart
                      data={topicData}
                      margin={{ top: 20, right: 30, left: 40, bottom: 60 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis
                        dataKey="topic"
                        angle={-45}
                        textAnchor="end"
                        interval={0}
                        height={80}
                      />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="count" fill="#1976d2" name="Number of Reviews" />
                    </BarChart>
                  </ResponsiveContainer>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Status Distribution Pie Chart */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Review Status Distribution
              </Typography>
              <Box sx={{ height: 300, width: '100%' }}>
                <PieChart
                  series={[
                    {
                      data: statusData,
                      highlightScope: { faded: 'global', highlighted: 'item' },
                      faded: { innerRadius: 30, additionalRadius: -30 },
                    },
                  ]}
                  height={300}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Additional Statistics */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Key Metrics
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={4}>
                  <Typography variant="subtitle1" color="textSecondary">
                    Total Reviews
                  </Typography>
                  <Typography variant="h4">
                    {stats.totalReviews}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="subtitle1" color="textSecondary">
                    Success Rate
                  </Typography>
                  <Typography variant="h4">
                    {Math.round((stats.totalReviews - stats.failedContent) / stats.totalReviews * 100)}%
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography variant="subtitle1" color="textSecondary">
                    Total Topics
                  </Typography>
                  <Typography variant="h4">
                    {Object.keys(stats.reviewsByTopic || {}).length}
                  </Typography>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Statistics; 