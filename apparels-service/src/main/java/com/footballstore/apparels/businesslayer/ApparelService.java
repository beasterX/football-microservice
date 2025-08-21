package com.footballstore.apparels.businesslayer;

import com.footballstore.apparels.presentationlayer.ApparelRequestModel;
import com.footballstore.apparels.presentationlayer.ApparelResponseModel;

import java.util.List;

public interface ApparelService {
    List<ApparelResponseModel> getAllApparels();
    ApparelResponseModel getApparelById(String apparelId);
    ApparelResponseModel createApparel(ApparelRequestModel requestModel);
    ApparelResponseModel updateApparel(String apparelId, ApparelRequestModel requestModel);
    void deleteApparel(String apparelId);

    int getStock(String apparelId);
    void decreaseStock(String apparelId, int quantity);
    void increaseStock(String apparelId, int quantity);
}
