import { NativeModules } from "react-native";

const { RNOwPay } = NativeModules;
const defaultOptions = {};

export default {
  /**
   * 注册微信支付
   * @param {应用唯一标识} appId
   */
  registerWxPay(appId) {
    RNOwPay.registerWxPay(appId);
  },

  /**
   * 配置支付宝Scheme
   * @param {*} scheme
   */
  configAlipayScheme(scheme) {
    RNOwPay.configAlipayScheme(scheme);
  },

  /**
   * 配置银联Scheme
   * @param {*} scheme
   */
  configUnpayScheme(scheme) {
    RNOwPay.configUnpayScheme(scheme);
  },

  /**
   * 配置银联模式
   * @param {模式：01:开发 00:正式} mode
   */
  configUnpayMode(mode) {
    RNOwPay.configUnpayMode(mode);
  },

  /**
   * 配置Braintree的Token
   * @param {*} clientToken
   */
  configBrainTreeClientToken(clientToken) {
    RNOwPay.configBrainTreeClientToken(clientToken);
  },

  /**
   * 获取支付后的信息
   * @param {参数} params
   * @param {完成回调} callback
   */
  fetchNonce(params, errorBlock, resultBlock) {
    const paramsObj = {
      ...defaultOptions,
      ...params
    };
    RNOwPay.fetchNonce(paramsObj, errorBlock, resultBlock);
  },

  /**
   * 发起支付请求
   * @param {支付信息} orderInfo
   * @param {支付平台，1:微信 2:支付宝 3:银联} platform
   * @param {完成回调} callback
   */
  pay(orderInfo, platform = 0, callback) {
    const paramsObj = {
      ...defaultOptions,
      ...orderInfo
    };
    RNOwPay.pay(paramsObj, platform, callback);
  }
};
