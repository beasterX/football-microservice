package com.footballstore.apparels.datamapperlayer;


import com.footballstore.apparels.dataaccesslayer.Apparel;
import com.footballstore.apparels.presentationlayer.ApparelRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ApparelRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "apparelIdentifier", ignore = true)
    })
    Apparel requestModelToEntity(ApparelRequestModel requestModel);
}
//