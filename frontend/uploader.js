const cloudinary = require('cloudinary').v2;
const fs = require('fs');
const path = require('path');

cloudinary.config({
  cloud_name: 'dw8eobcaf',
  api_key: '972951522568987',
  api_secret: 'jp_erqxYBm-1-3I_-7U-R7mZ0K8'
});

async function uploadFiles() {
  const files = process.argv.slice(2);
  const results = {};
  for (const file of files) {
    try {
      const result = await cloudinary.uploader.upload(file);
      results[path.basename(file)] = result.secure_url;
      console.log(`Uploaded ${file}: ${result.secure_url}`);
    } catch (error) {
      console.error(`Error uploading ${file}:`, error);
    }
  }
  // Also write to a file so we can easily collect results
  fs.writeFileSync('upload_results.json', JSON.stringify(results, null, 2), { flag: 'a' });
}

uploadFiles();
