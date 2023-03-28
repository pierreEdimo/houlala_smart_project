package com.example.marketplaceclient.services;

import com.example.marketplaceclient.exception.MarketplaceException;
import com.example.marketplaceclient.feign.*;
import com.example.marketplaceclient.model.*;
import com.example.marketplaceclient.model.dto.CreateProductDto;
import com.example.marketplaceclient.model.dto.ProductDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductServiceFeignClient feignClient;

    private final StockServiceFeignClient stockerServiceFeignClient;

    private final UploadServiceFeignClient uploadServiceFeignClient;

    private final LocationServiceFeignClient locationFeignClient;

    private final OrderServiceFeignClient orderServiceFeignClient;


    @Override
    public List<ProductDto> getAllProducts() {

        List<ProductDto> productDtoList = new ArrayList<>();
        List<ProductAdditionalInformation> infos;
        List<Product> productResponses;

        try {
            productResponses = this.feignClient.getAllProducts();
            infos = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error("errorMessage: " + e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product response : productResponses) {
            for (ProductAdditionalInformation info : infos) {
                if (response.getProductSku().equals(info.getProductSku())) {
                    productDtoList.add(this.toProductDto(response, info, 0));
                }
            }
        }

        return productDtoList;
    }

    @Override
    public ProductDto getProduct(String id) {
        Product product;
        ProductAdditionalInformation info;

        try {
            product = this.feignClient.getSingleProduct(id);
            info = this.stockerServiceFeignClient.getASingleInfo(product.getProductSku());
        } catch (MarketplaceException e) {
            log.error("errorMessage:" + e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        return this.toProductDto(product, info, 0);
    }

    @Override
    public List<ProductDto> searchProduct(String searchWord) {
        List<Product> productResponses;
        List<ProductDto> productDtoList = new ArrayList<>();
        List<ProductAdditionalInformation> infos;

        try {
            productResponses = this.feignClient.searchProduct(searchWord);
            infos = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product response : productResponses) {
            for (ProductAdditionalInformation info : infos) {
                if (response.getProductSku().equals(info.getProductSku())) {
                    productDtoList.add(this.toProductDto(response, info, 0));
                }
            }
        }

        return productDtoList;
    }

    @Override
    public List<ProductDto> getProductSoonToBeOutOfStock(String locationId) {
        List<Product> productResponses;
        List<ProductDto> productDtoList = new ArrayList<>();
        List<ProductAdditionalInformation> infos;

        try {
            productResponses = this.feignClient.getProductByLocationId(locationId, 0);
            infos = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product response : productResponses) {
            for (ProductAdditionalInformation info : infos) {
                if (response.getProductSku().equalsIgnoreCase(info.getProductSku())) {
                    productDtoList.add(this.toProductDto(response, info, 0));
                }
            }
        }

        return productDtoList
                .stream().filter((x) -> x.getQuantity() < 5)
                .collect(Collectors.toList());

    }

    @Override
    public Product deleteProduct(String id) {
        try {
            return this.feignClient.deleteProduct(id);
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }
    }

    @Override
    public Product editProduct(String id, CreateProduct newProduct) {
        try {
            return this.feignClient.editProduct(id, newProduct);
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }
    }

    @Override
    public void addProductToFavorite(String userId, String id) {
        try {
            this.feignClient.addProductToFavorite(userId, id);
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }
    }

    @Override
    public List<Product> getFavoritesProduct(String userId) {
        try {
            return this.feignClient.getFavoritesProduct(userId);
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }
    }

    @Override
    public List<ProductDto> getProductByLocationId(String locationId, int limit) {
        List<Product> productList;
        List<ProductAdditionalInformation> infos;
        List<ProductDto> productDtoList = new ArrayList<>();

        try {
            productList = this.feignClient.getProductByLocationId(locationId, limit);
            infos = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product product : productList) {
            for (ProductAdditionalInformation info : infos) {
                if (product.getProductSku().equalsIgnoreCase(info.getProductSku())) {
                    productDtoList.add(this.toProductDto(product, info, 0));
                }
            }
        }

        return productDtoList;
    }

    @Override
    public ProductDto getProductByIdAndIsFavorite(String id, String userId) {
        Product response;
        ProductAdditionalInformation additionalInformation;

        try {
            response = this.feignClient.getProductByIdAndIsFavorite(id, userId);
            additionalInformation = this.stockerServiceFeignClient.getASingleInfo(response.getProductSku());

        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        return this.toProductDto(response, additionalInformation, 0);
    }

    @Override
    public List<ProductDto> getProductsWithLimit(int limit) {

        List<ProductDto> productDtoList = new ArrayList<>();
        List<Product> productList;
        List<ProductAdditionalInformation> informations;

        try {
            productList = this.feignClient.getProductsWithLimit(limit);
            informations = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product product : productList) {
            for (ProductAdditionalInformation info : informations) {
                if (product.getProductSku().equalsIgnoreCase(info.getProductSku())) {
                    productDtoList.add(this.toProductDto(product, info, 0));
                }
            }
        }

        return productDtoList;
    }

    @Override
    public String createProduct(String createProduct, MultipartFile image) {

        CreateProductDto newProduct;
        String imageUrl;


        try {
            ObjectMapper objectMapper = new ObjectMapper();
            newProduct = objectMapper.readValue(createProduct, CreateProductDto.class);
        } catch (IOException io) {
            log.error(io.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, io.getMessage());
        }

        String productSku = this.skuGenerator(newProduct.getName(), newProduct.getOriginLabel(), newProduct.getLocationId());

        CreateProduct product = new CreateProduct(
                newProduct.getName(),
                newProduct.getDescription(),
                newProduct.getWeight(),
                newProduct.getSellingPrice(),
                newProduct.getLocationId(),
                productSku.toLowerCase(),
                newProduct.getCategoryId(),
                newProduct.getProductType()
        );

        ProductAdditionalInformation info = new ProductAdditionalInformation(
                productSku.toLowerCase(),
                newProduct.getQuantity(),
                newProduct.getBuyingPrice(),
                newProduct.getOriginLabel(),
                newProduct.getLocationId()
        );

        try {

            imageUrl = this.uploadServiceFeignClient.uploadProductThumbnail(image);
            product.setImageUrl(imageUrl);
            this.feignClient.addProduct(product);
            this.stockerServiceFeignClient.addInfo(info);

        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        return "createProduct has been succesfully created";
    }

    @Override
    public ProductDto getProductByNameAndIsFavorite(String name, String userId) {
        Product product;
        ProductAdditionalInformation information;

        try {
            product = this.feignClient.getProductByNameAndIsFavorite(name, userId);
            information = this.stockerServiceFeignClient.getASingleInfo(product.getProductSku());
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        return this.toProductDto(product, information, 0);
    }

    @Override
    public ProductDto getProductBySkuAndIsFavorite(String productSku, String userId) {
        Product product;
        ProductAdditionalInformation information;

        try {
            product = this.feignClient.getProductBySkuAndIsFavorite(productSku, userId);
            information = this.stockerServiceFeignClient.getASingleInfo(product.getProductSku());
        } catch (MarketplaceException ex) {
            log.error(ex.getMessage());
            throw new ResponseStatusException(ex.getHttpStatus(), ex.getMessage());
        }

        return this.toProductDto(product, information, 0);
    }

    @Override
    public List<ProductDto> getRandomProductsByLocationId(String locationId, int size) {
        List<Product> productList;
        List<ProductDto> productDtoList = new ArrayList<>();
        List<ProductAdditionalInformation> informations;

        try {
            productList = this.feignClient.getRandomProductsByLocationId(locationId, size);
            informations = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product product : productList) {
            for (ProductAdditionalInformation info : informations) {
                if (product.getProductSku().equalsIgnoreCase(info.getProductSku())) {
                    productDtoList.add(this.toProductDto(product, info, 0));
                }
            }
        }

        return productDtoList;
    }

    @Override
    public List<ProductDto> getProductsByType(String typeId, int limit) {
        List<Product> productList;
        List<ProductDto> productDtoList = new ArrayList<>();
        List<ProductAdditionalInformation> informations;

        try {
            productList = this.feignClient.getProductsByType(typeId, limit);
            informations = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product product : productList) {
            for (ProductAdditionalInformation info : informations) {
                if (product.getProductSku().equalsIgnoreCase(info.getProductSku())) {
                    productDtoList.add(this.toProductDto(product, info, 0));
                }
            }
        }

        return productDtoList;
    }

    @Override
    public List<ProductDto> getRandomProductsByCategoryId(String categoryId, int size) {
        List<Product> productList;
        List<ProductDto> productDtoList = new ArrayList<>();
        List<ProductAdditionalInformation> informations;

        try {
            productList = this.feignClient.getProductsByCategoryId(categoryId, size);
            informations = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product product : productList) {
            for (ProductAdditionalInformation info : informations) {
                if (product.getProductSku().equalsIgnoreCase(info.getProductSku())) {
                    productDtoList.add(this.toProductDto(product, info, 0));
                }
            }
        }

        return productDtoList;
    }

    @Override
    public List<ProductDto> getTopProductsByLocationId(String locationId) {
        List<Product> productList;
        List<ProductDto> productDtoList = new ArrayList<>();
        List<SellReport> sellReports;
        List<ProductAdditionalInformation> informations;

        try {
            productList = this.feignClient.getProductByLocationId(locationId, 0);
            sellReports = this.orderServiceFeignClient.getTopOrders(locationId);
            informations = this.stockerServiceFeignClient.getAllProductInfos();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        for (Product product : productList) {
            for (ProductAdditionalInformation info : informations) {
                for (SellReport report : sellReports) {
                    if (product.getProductSku().equalsIgnoreCase(report.get_id()) && product.getProductSku().equalsIgnoreCase(info.getProductSku())) {
                        productDtoList.add(this.toProductDto(product, info, report.getTotal()));
                    }
                }
            }
        }

        return productDtoList.stream().sorted(Comparator.comparing(ProductDto::getTotalSell)).collect(Collectors.toList());
    }

    @Override
    public long productTotalCount(String locationId) {
        try {
            return this.feignClient.getAllProducts().
                    stream().filter(product -> product.getLocationId().equalsIgnoreCase(locationId)).count();
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }
    }

    private ProductDto toProductDto(Product response, ProductAdditionalInformation additionalInformation, int totalSells) {

        Location location;

        try {
            location = this.locationFeignClient.getALocation(response.getLocationId());
        } catch (MarketplaceException e) {
            log.error(e.getMessage());
            throw new ResponseStatusException(e.getHttpStatus(), e.getMessage());
        }

        return new ProductDto(
                response.get_id(),
                response.getName(),
                response.getDescription(),
                response.getWeight(),
                response.getImageUrl(),
                response.getSellingPrice(),
                response.getLocationId(),
                response.isBookMarked(),
                additionalInformation.getQuantity(),
                additionalInformation.getArrivalDate(),
                additionalInformation.getBuyingPrice(),
                additionalInformation.getOriginLabel().getLabel(),
                additionalInformation.getProductSku(),
                location.getName(),
                totalSells
        );
    }

    private String skuGenerator(String productName,
                                String originLabel,
                                String locationId) {
        String result;
        LocalDateTime date = LocalDateTime.now();
        int year = date.getYear();
        int hour = date.getHour();
        int min = date.getMinute();
        String locationIdFirst3Chars = this.getThreeFirstChars(locationId);
        String productFirst3Chars = this.getThreeFirstChars(productName);
        String originFirst3Chars = this.getThreeFirstChars(originLabel);
        result = locationIdFirst3Chars + productFirst3Chars + originFirst3Chars + year + hour + min;
        return result.toUpperCase();
    }

    private String getThreeFirstChars(String str) {
        return str.substring(0, Math.min(str.length(), 3));
    }


}
