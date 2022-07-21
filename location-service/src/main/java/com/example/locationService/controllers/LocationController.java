package com.example.locationService.controllers;

import com.example.locationService.model.Location;
import com.example.locationService.model.LocationResponse;
import com.example.locationService.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LocationController {

    private final LocationService service;

    @GetMapping("/{id}")
    public LocationResponse getASingleLocation(@PathVariable long id) {
        return this.service.getLocation(id);
    }

    @PostMapping("")
    public Location addLocation(@RequestPart(name = "location") String newLocation, @RequestPart(name = "file") MultipartFile file) {
        return this.service.createNewLocation(newLocation, file);
    }

    @GetMapping("")
    public List<LocationResponse> getAllLocations() {
        return this.service.getAllLocations();
    }

    @PutMapping("/{id}")
    public Location editLocation(@RequestBody Location newLocation, @PathVariable long id) {
        return this.service.editLocation(newLocation, id);
    }

    @DeleteMapping("/{id}")
    public void deleteLocation(@PathVariable long id) {
        this.service.deleteLocation(id);
    }

    @GetMapping("/country/{id}")
    public List<LocationResponse> getLocationByCountryId(@PathVariable long id) {
        return this.service.getLocationsByCountryId(id);
    }

    @GetMapping("/uniqueIdentifier/{uid}")
    public LocationResponse getLocationByUniqueIdentifier(@PathVariable("uid") String uniqueIdentifier) {
        return this.service.getLocationByUniqueIdentifier(uniqueIdentifier);
    }
}
