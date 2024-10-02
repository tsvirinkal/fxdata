import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StateItemComponent } from './state-item.component';

describe('StateItemComponent', () => {
  let component: StateItemComponent;
  let fixture: ComponentFixture<StateItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StateItemComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StateItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
