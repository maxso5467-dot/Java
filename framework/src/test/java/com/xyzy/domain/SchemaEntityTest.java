package com.xyzy.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xyzy.domain.entity.Menu;
import com.xyzy.domain.entity.Role;
import com.xyzy.domain.entity.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SchemaEntityTest {

    @Test
    void mapsRbacAndTagEntitiesToExpectedTables() {
        assertEquals("t_role", Role.class.getAnnotation(TableName.class).value());
        assertEquals("t_menu", Menu.class.getAnnotation(TableName.class).value());
        assertEquals("t_tag", Tag.class.getAnnotation(TableName.class).value());
    }
}
