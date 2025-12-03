#!/bin/bash
# Upload content to server - Example script

# Configuration
SERVER="your-server.com"
REMOTE_PATH="/var/www/html/content"
LOCAL_CONTENT_DIR="./content"

echo "PickleTV Content Upload Script"
echo "==============================="

# Check if content directory exists
if [ ! -d "$LOCAL_CONTENT_DIR" ]; then
    echo "Creating content directory..."
    mkdir -p "$LOCAL_CONTENT_DIR"
fi

# Check if content.json exists
if [ ! -f "$LOCAL_CONTENT_DIR/content.json" ]; then
    echo "Creating sample content.json..."
    cat > "$LOCAL_CONTENT_DIR/content.json" << 'EOF'
{
  "videos": [
    {
      "id": "demo-1",
      "title": "Demo Video",
      "description": "Sample video for testing",
      "thumbnailUrl": "https://tv.dilly.cloud/content/demo-thumb.jpg",
      "videoUrl": "https://tv.dilly.cloud/content/demo-video.mp4",
      "duration": "1:00",
      "category": "Demo"
    }
  ]
}
EOF
    echo "Created content.json template"
fi

echo ""
echo "Content directory: $LOCAL_CONTENT_DIR"
echo "Files to upload:"
ls -lh "$LOCAL_CONTENT_DIR"

echo ""
read -p "Upload to $SERVER:$REMOTE_PATH? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Uploading..."
    scp -r "$LOCAL_CONTENT_DIR"/* "$SERVER:$REMOTE_PATH/"
    echo "Upload complete!"
    echo ""
    echo "Verify at: https://tv.dilly.cloud/content/content.json"
else
    echo "Upload cancelled"
fi

