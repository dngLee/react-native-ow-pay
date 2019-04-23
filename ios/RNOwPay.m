
#import "RNOwPay.h"
#import <React/RCTUtils.h>
#import "WXApi.h"
#import "WXApiObject.h"
#import <AlipaySDK/AlipaySDK.h>
#import "UPPaymentControl.h"
//#import <Braintree/BTAPIClient.h>
//#import <Braintree/BTCardClient.h>
//#import <Braintree/BTCard.h>

@interface RNOwPay() <WXApiDelegate> {
    NSString * _alipayScheme;
    NSString * _unpayScheme;
    NSString * _unpayMode;
    NSString * _clientToken;
    RCTResponseSenderBlock _callback;
}

@end

@implementation RNOwPay

- (instancetype)init {
    if (self = [super init]) {
        _unpayMode = @"01";
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleOpenURL:) name:@"RCTOpenURLNotification" object:nil];
    }
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (BOOL)handleOpenURL:(NSNotification *)aNotification {
    NSString * aURLString =  [aNotification userInfo][@"url"];
    NSURL * aURL = [NSURL URLWithString:aURLString];
    
    if ([aURL.host isEqualToString:@"safepay"]) {
        //跳转支付宝钱包进行支付，处理支付结果
        [[AlipaySDK defaultService] processOrderWithPaymentResult:aURL standbyCallback:^(NSDictionary *resultDic) {
            [self callback:resultDic];
        }];
    }
    
    if ([aURL.host isEqualToString:@"uppayx1"] || [aURL.host isEqualToString:@"uppaywallet"]) {
        [[UPPaymentControl defaultControl] handlePaymentResult:aURL completeBlock:^(NSString *code, NSDictionary *data) {
            NSMutableDictionary * resultDic = data.mutableCopy;
            resultDic[@"code"] = code;
            [self callback:resultDic];
        }];
    }
    
    return [WXApi handleOpenURL:aURL delegate:self];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE(RNOwPay)

RCT_EXPORT_METHOD(registerWxPay:(NSString *)appId) {
    [WXApi registerApp:appId enableMTA:YES];
}

RCT_EXPORT_METHOD(configAlipayScheme:(NSString *)scheme) {
    _alipayScheme = scheme;
}

RCT_EXPORT_METHOD(configUnpayScheme:(NSString *)scheme) {
    _unpayScheme = scheme;
}

RCT_EXPORT_METHOD(configUnpayMode:(NSString *)mode) {
    _unpayMode = mode;
}

RCT_EXPORT_METHOD(configBrainTreeClientToken:(NSString *)clientToken) {
    _clientToken = clientToken;
}

RCT_EXPORT_METHOD(pay:(NSDictionary *)orderInfo platform:(NSInteger)platform callback:(RCTResponseSenderBlock)callback) {
    _callback = callback;
    switch (platform) {
        case 0: { //微信
            PayReq* req = [[PayReq alloc] init];
            req.partnerId = [orderInfo objectForKey:@"partnerid"];
            req.prepayId = [orderInfo objectForKey:@"prepayid"];
            req.nonceStr = [orderInfo objectForKey:@"noncestr"];
            req.timeStamp = [orderInfo[@"timestamp"] doubleValue];
            req.package = [orderInfo objectForKey:@"package"];
            req.sign = [orderInfo objectForKey:@"sign"];
            //发送请求到微信，等待微信返回onResp
            dispatch_async(dispatch_get_main_queue(), ^{
                [WXApi sendReq:req];
            });
        }
            break;
        case 1: { //支付宝
            NSString * orderString = orderInfo[@"orderString"];
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AlipaySDK defaultService] payOrder:orderString fromScheme:self->_alipayScheme callback:^(NSDictionary *resultDic) {
                    callback(@[resultDic]);
                }];
            });
        }
            break;
        case 2: { //银联
            NSString * tn = orderInfo[@"tn"];
            BOOL callResult = [[UPPaymentControl defaultControl] startPay:tn fromScheme:_unpayScheme mode:_unpayMode viewController:RCTPresentedViewController()];
            if (callback) {
                callback(@[@{@"callResult": callResult ? @"Call Succeed" : @"Call Failed"}]);
            }
        }
            break;
        default: {
            if (callback) {
                callback(@[@"平台类型不正确，0：微信 1：支付宝 2：银联"]);
            }
        }
            break;
    }

}

//RCT_EXPORT_METHOD(fetchNonce:(NSDictionary *)params
//                  errorBlock:(RCTResponseErrorBlock)errorBlock
//                 resultBlock:(RCTResponseSenderBlock)resultBlock) {
//    NSString * holderName = params[@"holderName"];
//    NSString * cardNumber = params[@"cardNumber"];
//    NSString * month = params[@"month"];
//    NSString * year = params[@"year"];
//    NSString * cvv = params[@"cvv"];
//
//    BTAPIClient *braintreeClient = [[BTAPIClient alloc] initWithAuthorization:_clientToken];
//    BTCardClient *cardClient = [[BTCardClient alloc] initWithAPIClient:braintreeClient];
//
//    BTCard *card = [[BTCard alloc] initWithNumber:cardNumber
//                                  expirationMonth:month
//                                   expirationYear:year
//                                              cvv:cvv];
//    card.cardholderName = holderName;
//
//    [cardClient tokenizeCard:card
//                  completion:^(BTCardNonce *tokenizedCard, NSError *error) {
//                      if (error != nil) {
//                          errorBlock(error);
//                      } else {
//                          resultBlock(@[@{@"last2": tokenizedCard.lastTwo,
//                                          @"type": tokenizedCard.type,
//                                          @"description": tokenizedCard.localizedDescription,
//                                          @"nonce": tokenizedCard.nonce}]);
//                      }
//                  }];
//}

- (void)callback:(id)data {
    if (_callback) {
        _callback(@[data]);
        _callback = nil;
    }
}

#pragma mark - WXApiDelegate

- (void)onReq:(BaseReq *)req {
    NSLog(@"BaseReq: %@", req);
}

- (void)onResp:(BaseResp *)resp {
    if ([resp isKindOfClass:[PayResp class]]) {
        NSDictionary *reqDict = @{@"errStr": resp.errStr ?: @"",
                                  @"type": @(resp.type?: -99999) ,
                                  @"errCode": @(resp.errCode)};
        [self callback:reqDict];
    }
}

@end
  
