package com.udacity.catpoint.service;

import com.udacity.catpoint.data.AlarmStatus;
import com.udacity.catpoint.data.ArmingStatus;
import com.udacity.catpoint.data.SecurityRepository;
import com.udacity.catpoint.data.Sensor;
import com.udacity.catpoint.data.SensorType;
import com.udacity.image.service.ImageService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    @InjectMocks
    private SecurityService securityService;

    @Test
    void alarmGoesPendingWhenSensorActivated() {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class,
            names = {"ARMED_HOME", "ARMED_AWAY"})
    void sensorActivatedWhenSystemArmedSetsPendingAlarm(ArmingStatus armingStatus) {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getArmingStatus())
                .thenReturn(armingStatus);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.NO_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void alarmGoesToAlarmStateWhenSensorActivatedAgain() {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.ALARM);

    }

    @Test
    void pendingAlarmReturnsToNoAlarmWhenAllSensorsInactive() {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void alarmStateDoesNotChangeWhenActive() {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository, Mockito.never())
                .setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void sensorActivatedWhileAlreadyActiveAndPendingChangesToAlarm() {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void inactiveSensorDoesNotChangeAlarmState() {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(false);

        securityService.changeSensorActivationStatus(sensor, false);

        Mockito.verify(securityRepository, Mockito.never())
                .setAlarmStatus(Mockito.any());
    }

    @Test
    void catDetectedWhenArmedHomeSetsAlarm() {

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(imageService.imageContainsCat(Mockito.any(), Mockito.anyFloat()))
                .thenReturn(true);

        securityService.processImage(null);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void noCatSetsNoAlarmWhenNoSensorsActive() {

        when(imageService.imageContainsCat(Mockito.any(), Mockito.anyFloat()))
                .thenReturn(false);

        securityService.processImage(null);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void disarmingSystemSetsNoAlarm() {

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void armedHomeWithCatSetsAlarm() {

        when(securityRepository.getArmingStatus())
                .thenReturn(ArmingStatus.ARMED_HOME);

        when(imageService.imageContainsCat(Mockito.any(), Mockito.anyFloat()))
                .thenReturn(true);

        securityService.processImage(null);

        Mockito.verify(securityRepository)
                .setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void activeAlarmDoesNotChangeWhenSensorDeactivated() {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.ALARM);

        securityService.changeSensorActivationStatus(sensor, false);

        Mockito.verify(securityRepository, Mockito.never())
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void armingSystemResetsAllSensors() {

        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        Mockito.verify(securityRepository)
                .setArmingStatus(ArmingStatus.ARMED_HOME);
    }

    @Test
    void catRemovedButSensorActiveKeepsAlarm() {

        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(true);

        when(securityRepository.getAlarmStatus())
                .thenReturn(AlarmStatus.ALARM);

        when(imageService.imageContainsCat(Mockito.any(), Mockito.anyFloat()))
                .thenReturn(false);

        securityService.processImage(null);

        Mockito.verify(securityRepository, Mockito.never())
                .setAlarmStatus(AlarmStatus.NO_ALARM);
    }
}