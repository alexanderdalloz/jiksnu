// var config = require('cukefarm').config;

var config2 = {
  // set to "custom" instead of cucumber.
  framework: 'custom',
  seleniumAddress: 'http://selenium:24444/wd/hub',
  // path relative to the current config file
  frameworkPath: require.resolve('protractor-cucumber-framework'),

  specs: ['features/*.feature'],
  baseUrl: 'http://jiksnu-dev:8080/',
  capabilities: {
    browserName: 'chrome',
    'phantomjs.binary.path': require('phantomjs-prebuilt').path
  },
  cucumberOpts: {
    format: 'pretty',
    require: [
      // 'spec.js',
      // 'main.js',
      // 'target/resources/public/c'
      'target/protractor-tests.js'
      // ,
      // 'target/specs/jiksnu/step_definitions.js'
    ]
  }
};

exports.config = config2;
