package com.tournament.domain.repository;

import com.tournament.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de dominio para la entidad Category
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Busca una categoría por su código
     * @param code Código de la categoría
     * @return Categoría encontrada
     */
    Optional<Category> findByCode(String code);

    /**
     * Busca categorías activas
     * @return Lista de categorías activas
     */
    List<Category> findByIsActiveTrue();

    /**
     * Verifica si existe una categoría con el código dado
     * @param code Código de la categoría
     * @return true si existe
     */
    boolean existsByCode(String code);

    /**
     * Busca categorías por alias
     * @param alias Alias de la categoría
     * @return Lista de categorías
     */
    List<Category> findByAlias(String alias);
} 