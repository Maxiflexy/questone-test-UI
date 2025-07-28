# Use official Node.js runtime as base image
FROM node:18-alpine

# Set working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json (if available)
# This is done before copying other files to leverage Docker layer caching
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the rest of the application code
COPY . .

# Create a non-root user for security
RUN addgroup -g 1001 -S nodejs
RUN adduser -S reactuser -u 1001

# Change ownership of the app directory to the non-root user
RUN chown -R reactuser:nodejs /app

# Switch to non-root user
USER reactuser

# Expose the port the app runs on
EXPOSE 3000

# Set environment to development
ENV NODE_ENV=development

# Command to run the application
CMD ["npm", "start"]