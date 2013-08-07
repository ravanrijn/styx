package com.github.styx.controllers;

import com.github.styx.domain.Space;
import com.github.styx.domain.SpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/api")
public class SpaceController {

    private final SpaceRepository spaceRepository;

    @Autowired
    public SpaceController(SpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    @RequestMapping(value = "/spaces/{id}", method = DELETE)
    @ResponseBody
    public void deleteSpaceById(@RequestHeader("Authorization") final String token, @PathVariable("id") final String id) {
        spaceRepository.deleteById(token, id);
    }

    @RequestMapping(value = "/spaces/{id}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Space getSpaceById(@RequestHeader("Authorization") final String token, @PathVariable("id") final String id) {
        return spaceRepository.getById(token, id);
    }

    @RequestMapping(value = "/organizations/{id}/spaces", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Space> getSpacesByOrganizationId(@RequestHeader("Authorization") final String token, @PathVariable("id") final String id) {
        return spaceRepository.getByOrganizationId(token, id);
    }

}
