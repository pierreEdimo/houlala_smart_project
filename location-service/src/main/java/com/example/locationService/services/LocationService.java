package com.example.locationService.services;

import com.example.locationService.model.Location;
import com.example.locationService.model.LocationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LocationService {

    LocationResponse getLocation(long id);

    Location createNewLocation(String location, MultipartFile file);

    List<LocationResponse> getAllLocations();

    Location editLocation(Location location, long id);

    void deleteLocation(long id);

    List<LocationResponse> getLocationsByCountryId(long id);

    LocationResponse getLocationByUniqueIdentifier(String uniqueIdentifier);

    List<LocationResponse> getStores(Long limit);

    List<LocationResponse> getStoreByCategoryId(long categoryId);

    List<LocationResponse> getLocations();

    List<LocationResponse> filterStoreByName(String searchWord);
}
