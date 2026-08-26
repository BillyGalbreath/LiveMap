const path = require("path");
const CssMinimizerPlugin = require("css-minimizer-webpack-plugin");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const TerserPlugin = require("terser-webpack-plugin");
const CopyPlugin = require("copy-webpack-plugin");

// noinspection JSUnusedGlobalSymbols
module.exports = {
  devServer: {
    historyApiFallback: {
      publicPath: "/",
      rewrites: [
        {
          from: /^\/(.+?)(?:\/(.+?)?\/?(-?\d+)?\/?(-?\d+)?\/?(-?\d+)?(?:\/(.+)?)?)?$/,
          to: ctx => "/" + (ctx.match[6] ? ctx.match[6] : "index.html")
        },
        {
          from: /./,
          to: "/404.html"
        }
      ]
    }
  },
  devtool: "source-map", // comment out for production
  entry: "./src/LiveMap.ts",
  externals: {
    "leaflet": "L"
  },
  mode: "production",
  module: {
    rules: [
      {
        test: /\.ts$/,
        use: "ts-loader",
        include: [path.resolve(__dirname, "src")]
      },
      {
        test: /\.css$/,
        use: [
          {
            loader: MiniCssExtractPlugin.loader,
            options: {
              esModule: false
            }
          },
          {
            loader: "css-loader",
            options: {
              sourceMap: true,
              url: false,
            }
          }
        ],
      },
      {
        test: /\.svg$/,
        loader: "svg-sprite-loader",
        options: {
          symbolId: (filePath) => `icon-${path.basename(filePath, ".svg")}`,
        },
      }
    ]
  },
  optimization: {
    minimize: false, // enable for production
    minimizer: [
      new CssMinimizerPlugin(),
      new TerserPlugin({
        extractComments: false,
        terserOptions: {
          format: {
            comments: false
          }
        }
      })
    ]
  },
  output: {
    publicPath: "/",
    filename: "livemap.js",
    path: path.resolve(__dirname, "dist")
  },
  performance: {
    maxEntrypointSize: 1024000,
    maxAssetSize: 1024000
  },
  resolve: {
    extensions: [".ts", ".js", ".css"]
  },
  stats: "errors-only",
  plugins: [
    new CopyPlugin({
      patterns: [
        {
          from: "public",
          globOptions: {
            ignore: ["**/tiles/**"]
          }
        }
      ]
    }),
    new MiniCssExtractPlugin({
      filename: "livemap.css"
    })
  ]
}
