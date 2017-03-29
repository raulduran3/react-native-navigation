const exec = require('shell-utils').exec;

run();

function run() {
  exec.kill('8081');
  exec.execSync(`watchman watch-del-all || true`);
  exec.execSync(`adb reverse tcp:8081 tcp:8081 || true`);
  exec.execSync(`node ./node_modules/react-native/local-cli/cli.js start --root=./playground`);
}

// function findPort() {
//   let port = 8081;
//   if (!process.env.CI) {
//     return port;
//   }
//   while (!isPortOpen(port)) {
//     port = Number((Math.random() * 1000).toFixed(0)) + 35000;
//   }
//   return port;
// }

// function isPortOpen(port) {
//   try {
//     return !exec.execSyncRead(`netstat -vanp tcp | grep ${port}`);
//   } catch (e) {
//     return true;
//   }
// }
