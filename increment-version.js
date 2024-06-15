const fs = require('fs');
const path = require('path');

const configFilePath = path.resolve(__dirname, 'openapitools.json');

function getCurrentVersion() {
    const config = JSON.parse(fs.readFileSync(configFilePath, 'utf-8'));
    return config['generator-cli'].generators['kotlin-todo-api'].additionalProperties.npmVersion;
}

function incrementVersion(version) {
    const parts = version.split('.').map(Number);
    parts[2] += 1; // Increment the patch version (e.g., 1.0.2 -> 1.0.3)
    return parts.join('.');
}

function updateConfig(newVersion) {
    const config = JSON.parse(fs.readFileSync(configFilePath, 'utf-8'));
    config['generator-cli'].generators['kotlin-todo-api'].additionalProperties.npmVersion = newVersion;
    fs.writeFileSync(configFilePath, JSON.stringify(config, null, 2));
}

function main() {
    const currentVersion = getCurrentVersion();
    const newVersion = incrementVersion(currentVersion);

    updateConfig(newVersion);
    console.log(`Updated version from ${currentVersion} to ${newVersion}`);
}

main();