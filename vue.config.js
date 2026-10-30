module.exports = {
  publicPath: './',
  productionSourceMap: false,
  devServer: {
    host: '127.0.0.1', port: 7000,
    proxy: { '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true } }
  },
  configureWebpack: { performance: { hints: false } }
}
