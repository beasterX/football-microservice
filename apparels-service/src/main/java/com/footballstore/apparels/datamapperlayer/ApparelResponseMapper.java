package com.footballstore.apparels.datamapperlayer;

import com.footballstore.apparels.dataaccesslayer.Apparel;
import com.footballstore.apparels.presentationlayer.ApparelController;
import com.footballstore.apparels.presentationlayer.ApparelResponseModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Mapper(componentModel = "spring")
public interface ApparelResponseMapper {

    @Mapping(source = "apparelIdentifier.apparelId", target = "apparelId")
    ApparelResponseModel entityToResponseModel(Apparel apparel);

    List<ApparelResponseModel> entityListToResponseModelList(List<Apparel> apparels);
    
}
