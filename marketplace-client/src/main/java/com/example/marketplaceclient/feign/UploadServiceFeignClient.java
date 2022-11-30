package com.example.marketplaceclient.feign;

import com.example.marketplaceclient.config.FeignSupportConfig;
import com.example.marketplaceclient.exception.MarketplaceException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;


@FeignClient(url = "https://upload.houlala.store", name = "upload", configuration = FeignSupportConfig.class)
public interface UploadServiceFeignClient {

    @PostMapping(value = "/api/Upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadImage(@RequestPart("image") MultipartFile image) throws MarketplaceException;

}
