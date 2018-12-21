package cam72cam.immersiverailroading.sound;

import cam72cam.immersiverailroading.util.math.Vec3d;

public interface ISound {
	public void play(Vec3d pos);
	public void stop();
	public void update();
	public void terminate();
	public void setPosition(Vec3d pos);
	public void setPitch(float f);
	public void setVelocity(Vec3d vel);
	public void setVolume(float f);
	public boolean isPlaying();
	void updateBaseSoundLevel(float baseSoundMultiplier);
	void reload();
	public void disposable();
	public boolean isDisposable();
}
