package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethodRepository;
import org.springframework.stereotype.Service;

/**
 * 特徴サービス
 */
@Service
public class CharacteristicService {

    CharacterizedMethodRepository characterizedMethodRepository;

    public CharacteristicService(CharacterizedMethodRepository characterizedMethodRepository) {
        this.characterizedMethodRepository = characterizedMethodRepository;
    }

}
