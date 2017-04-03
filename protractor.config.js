// var config = require('cukefarm').config;

var config2 = {
  // set to "custom" instead of cucumber.
  framework: 'custom',

  // path relative to the current config file
  frameworkPath: require.resolve('protractor-cucumber-framework'),

  specs: ['features/*.feature'],
  baseUrl: 'http://web/',
  capabilities: {
    browserName: 'phantomjs',
    'phantomjs.binary.path': require('phantomjs-prebuilt').path
  },
  cucumberOpts: {
    format: 'pretty',
    require: [
      'spec.js',
      // 'main.js',
      // 'target/resources/public/c'
      'target/protractor-tests.js'
    ]
  }

};

exports.config = config2;
