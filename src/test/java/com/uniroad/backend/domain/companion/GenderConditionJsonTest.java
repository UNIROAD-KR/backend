package com.uniroad.backend.domain.companion;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.uniroad.backend.domain.companion.entity.GenderCondition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GenderConditionJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 응답에는_한글_문구로_나간다() throws Exception {
        assertThat(objectMapper.writeValueAsString(GenderCondition.ANY)).isEqualTo("\"무관\"");
        assertThat(objectMapper.writeValueAsString(GenderCondition.FEMALE_ONLY)).isEqualTo("\"여성만\"");
        assertThat(objectMapper.writeValueAsString(GenderCondition.MALE_ONLY)).isEqualTo("\"남성만\"");
    }

    @Test
    void 요청의_한글_문구를_enum으로_받는다() throws Exception {
        assertThat(objectMapper.readValue("\"무관\"", GenderCondition.class)).isEqualTo(GenderCondition.ANY);
        assertThat(objectMapper.readValue("\"여성만\"", GenderCondition.class)).isEqualTo(GenderCondition.FEMALE_ONLY);
        assertThat(objectMapper.readValue("\"남성만\"", GenderCondition.class)).isEqualTo(GenderCondition.MALE_ONLY);
    }

    @Test
    void 예전_enum_이름도_받는다() throws Exception {
        assertThat(objectMapper.readValue("\"FEMALE_ONLY\"", GenderCondition.class)).isEqualTo(GenderCondition.FEMALE_ONLY);
    }

    @Test
    void 없는_값은_거부한다() {
        assertThatThrownBy(() -> objectMapper.readValue("\"여자만\"", GenderCondition.class))
                .isInstanceOf(ValueInstantiationException.class);
        assertThatThrownBy(() -> GenderCondition.from("여자만"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 빈_값은_조건_없음으로_본다() {
        assertThat(GenderCondition.from(null)).isNull();
        assertThat(GenderCondition.from(" ")).isNull();
    }
}
