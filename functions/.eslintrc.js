module.exports = {
  env: {
    es6: true,
    node: true,
  },
  extends: "eslint:recommended",
  rules: {
    "indent": ["error", 2],
    "no-unused-vars": "off",
    "max-len": ["error", { "code": 100 }],
  },
  parserOptions: {
    ecmaVersion: 8,
  },
};
