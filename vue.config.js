module.exports = {
  publicPath: './',
  productionSourceMap: false,
  devServer: { host: '127.0.0.1', port: 7000 },
  configureWebpack: { performance: { hints: false } }
}
