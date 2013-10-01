package com.github.styx.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.apache.commons.io.FileUtils.readFileToString;

@Service
class LocalChuckNorrisQuoter implements ChuckNorrisQuoter {

    private final List<ChuckNorrisQuote> chuckNorrisQuotes;

    @Autowired
    public LocalChuckNorrisQuoter(final ObjectMapper objectMapper) throws IOException {
        chuckNorrisQuotes = objectMapper.readValue(readFileToString(new ClassPathResource("/chucknorris.json", this.getClass().getClassLoader()).getFile()), objectMapper.getTypeFactory().constructCollectionType(List.class, ChuckNorrisQuote.class));
    }

    @Override
    public String randomQuote() {
        return chuckNorrisQuotes.get(new Random().nextInt(chuckNorrisQuotes.size() - 1)).getQuote();
    }

    public static class ChuckNorrisQuote {
        private final int id;
        private final String quote;
        private final List<String> categories;

        @JsonCreator
        public ChuckNorrisQuote(@JsonProperty("id") int id, @JsonProperty("joke") String quote, @JsonProperty("categories") List<String> categories) {
            this.id = id;
            this.quote = quote;
            this.categories = categories;
        }

        public int getId() {
            return id;
        }

        public String getQuote() {
            return quote;
        }

        public List<String> getCategories() {
            return categories;
        }

    }

}
