package com.footballstore.apigateway.businesslayer.apparels;

import com.footballstore.apigateway.presentationlayer.apparels.ApparelRequestModel;
import com.footballstore.apigateway.presentationlayer.apparels.ApparelResponseModel;
import java.util.List;

public interface ApparelsService {
    List<ApparelResponseModel> getAllApparels();
    ApparelResponseModel getApparelById(String apparelId);
    ApparelResponseModel createApparel(ApparelRequestModel requestModel);
    ApparelResponseModel updateApparel(String apparelId, ApparelRequestModel requestModel);
    ApparelResponseModel deleteApparel(String apparelId);
}
