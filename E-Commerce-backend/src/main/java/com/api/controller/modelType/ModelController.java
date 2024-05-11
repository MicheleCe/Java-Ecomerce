package com.api.controller.modelType;

import com.model.ModelType;
import com.repository.ModelTypeDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/model")
public class ModelController {

    private final ModelTypeDAO modelTypeDAO;

    public ModelController(ModelTypeDAO ModelTypeDAO) {
        this.modelTypeDAO = ModelTypeDAO;
    }


    @GetMapping
    public ResponseEntity<List<ModelType>> getAllModelTypes() {
        List<ModelType> modelTypes = modelTypeDAO.findAll();
        return ResponseEntity.ok(modelTypes);
    }

    @PostMapping
    public ResponseEntity<ModelType> createModelType(@RequestBody ModelType modelType) {
        ModelType createdModelType = modelTypeDAO.save(modelType);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdModelType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModelType> updateModelType(@PathVariable UUID id, @RequestBody ModelType modelType) {
        if (modelTypeDAO.existsById(id)) {
            modelType.setId(id); 
            ModelType updatedModelType = modelTypeDAO.save(modelType);
            return ResponseEntity.ok(updatedModelType);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModelType(@PathVariable UUID id) {
        if (modelTypeDAO.existsById(id)) {
            modelTypeDAO.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
