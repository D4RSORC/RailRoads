package cam72cam.immersiverailroading.track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cam72cam.immersiverailroading.library.SwitchState;
import cam72cam.immersiverailroading.library.TrackDirection;
import org.apache.commons.lang3.tuple.Pair;

import cam72cam.immersiverailroading.util.RailInfo;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.immersiverailroading.util.math.BlockPos;
import cam72cam.immersiverailroading.util.math.Vec3d;

public abstract class BuilderIterator extends BuilderBase implements IIterableTrack {
	protected HashSet<Pair<Integer, Integer>> positions;
	
	public BuilderIterator(RailInfo info, BlockPos pos) {
		this(info, pos, false);
	}

	public abstract List<PosStep> getPath(double stepSize);

	public BuilderIterator(RailInfo info, BlockPos pos, boolean endOfTrack) {
		super(info, pos);
		
		positions = new HashSet<Pair<Integer, Integer>>();
		HashMap<Pair<Integer, Integer>, Float> bedHeights = new HashMap<Pair<Integer, Integer>, Float>();
		HashMap<Pair<Integer, Integer>, Float> railHeights = new HashMap<Pair<Integer, Integer>, Float>();
		HashMap<Pair<Integer, Integer>, Integer> yOffset = new HashMap<Pair<Integer, Integer>, Integer>();
		HashSet<Pair<Integer, Integer>> flexPositions = new HashSet<Pair<Integer, Integer>>();
		
		double horiz = info.settings.gauge.value()/2;
		if (info.settings.isGradeCrossing) {
			horiz += 2f * info.settings.gauge.scale();
		}
		double clamp = 0.17 * info.settings.gauge.scale();
		float heightOffset = (float) ((info.placementInfo.placementPosition.y) % 1);

		List<PosStep> path = getPath(0.25);
		PosStep start = path.get(0);
		PosStep end = path.get(path.size()-1);

		int mainX = (int) Math.round(path.get(path.size()/2).x);
		int mainZ = (int) Math.round(path.get(path.size()/2).z);
		int flexDist = (int) Math.max(1, 3 * (0.5 + info.settings.gauge.scale()/2));

		for (PosStep cur : path) {
			Vec3d gagPos = cur;

			boolean isFlex = gagPos.distanceTo(start) < flexDist || gagPos.distanceTo(end) < flexDist;

			gagPos = gagPos.addVector(0, heightOffset, 0);

			for (double q = -horiz; q <= horiz; q+=0.1) {
				Vec3d nextUp = VecUtil.fromYaw(q, 90 + cur.yaw);
				int posX = (int)Math.round((gagPos.x+nextUp.x));
				int posZ = (int)Math.round((gagPos.z+nextUp.z));
				double height = 0;
				if (info.settings.isGradeCrossing) {
					height = (1 - Math.abs((int)q)/horiz)/3 - 0.05;
					height *= info.settings.gauge.scale();
					height = Math.min(height, clamp);
				}

				double relHeight = gagPos.y % 1;
				if (gagPos.y < 0) {
					relHeight += 1;
				}

				Pair<Integer, Integer> gag = Pair.of(posX, posZ);
				if (!positions.contains(gag)) {
					positions.add(gag);
                    bedHeights.put(gag, (float)(height + Math.max(0, relHeight - 0.1)));
                    railHeights.put(gag, (float) relHeight);
					yOffset.put(gag, (int) (gagPos.y - relHeight));
				}
				if (isFlex || Math.abs(q) > info.settings.gauge.value()) {
					flexPositions.add(gag);
				}
			}
			if (!isFlex && endOfTrack) {
				mainX = (int) Math.round(gagPos.x);
				mainZ = (int) Math.round(gagPos.z);
			}
		}

		BlockPos mainPos = new BlockPos(mainX, yOffset.get(Pair.of(mainX, mainZ)), mainZ);
		this.setParentPos(mainPos);
		TrackRail main = new TrackRail(this, mainPos	);
		tracks.add(main);
		main.setRailHeight(railHeights.get(Pair.of(mainX, mainZ)));
		main.setBedHeight(bedHeights.get(Pair.of(mainX, mainZ)));

		for (Pair<Integer, Integer> pair : positions) {
			if (pair.getLeft() == mainX && pair.getRight() == mainZ) {
				// Skip parent block
				continue;
			}
			TrackBase tg = new TrackGag(this, new BlockPos(pair.getLeft(), yOffset.get(pair), pair.getRight()));
			if (flexPositions.contains(pair)) {
				tg.setFlexible();
			}
			tg.setRailHeight(railHeights.get(pair));
			tg.setBedHeight(bedHeights.get(pair));
			tracks.add(tg);
		}
	}
	
	@Override
	public List<TrackBase> getTracksForRender() {
		return this.tracks;
	}

	private static float delta(float a, float b) {
		float angle = (float) Math.toDegrees(Math.toRadians(a) - Math.toRadians(b));
		if (angle > 180) {
			angle -= 360;
		}
		if (angle<-180) {
			angle += 360;
		}
		return angle;
	}

	@Override
	public List<VecYawPitch> getRenderData() {
		List<VecYawPitch> data = new ArrayList<VecYawPitch>();

		double scale = info.settings.gauge.scale();
		List<PosStep> points = getPath(scale);

		boolean switchStraight = info.switchState == SwitchState.STRAIGHT;
		int switchSize = 0;
		TrackDirection guessDirection = info.placementInfo.direction;
		if (switchStraight ) {
			for (int i = 0; i < points.size(); i++) {
				PosStep cur = points.get(i);
				Vec3d flatPos = VecUtil.rotateYaw(cur, info.placementInfo.yaw);
				if (Math.abs(flatPos.z) >= 0.5 * scale) {
					switchSize = i;
					guessDirection = cur.yaw - info.placementInfo.yaw > 0 ? TrackDirection.RIGHT : TrackDirection.LEFT;
					break;
				}
			}
		}

		for (int i = 0; i < points.size(); i++) {
			PosStep cur = points.get(i);
			PosStep switchPos = cur;
			if (switchStraight ) {
				double switchOffset = 1 - (i / (double)switchSize);
				if (switchOffset > 0) {
					Vec3d offset = VecUtil.fromYaw(0.2, cur.yaw + 90 + info.placementInfo.direction.toYaw());
					offset = offset.scale(switchOffset * scale);
					float offsetAngle = (float) (switchSize / 3.14 * scale); // Close enough for now
					if (guessDirection == TrackDirection.RIGHT)  {
						offsetAngle = -offsetAngle;
					}
					switchPos = new PosStep(cur.add(offset), cur.yaw + offsetAngle);
				}
			}
			
			float pitch = 0;
			float angle = 0;
			if (points.size() == 1) {
				//NOP
			} else if (i+1 == points.size()) {
				PosStep next = points.get(i-1);
				double ydelt = next.y - cur.y;
				double dist = next.distanceTo(cur);
				pitch = (float) Math.toDegrees(Math.atan2(ydelt, dist));
				angle = delta(next.yaw, cur.yaw);
				angle *= 2;
			} else if (i == 0) {
				PosStep next = points.get(i+1);
				double ydelt = next.y - cur.y;
				double dist = next.distanceTo(cur);
				pitch = (float) -Math.toDegrees(Math.atan2(ydelt, dist));
				angle = delta(cur.yaw, next.yaw);
				angle *= 2;
			} else {
				PosStep prev = points.get(i-1);
				PosStep next = points.get(i+1);
				double ydelt = next.y - prev.y;
				double dist = next.distanceTo(prev);
				pitch = (float) -Math.toDegrees(Math.atan2(ydelt, dist));
				angle = delta(prev.yaw, next.yaw);
			}
			if (angle != 0) {
				if (guessDirection == TrackDirection.RIGHT) {
					data.add(new VecYawPitch(switchPos.x, switchPos.y, switchPos.z, switchPos.yaw, pitch, (1 - angle / 180) * (float) info.settings.gauge.scale(), "RAIL_LEFT"));
					data.add(new VecYawPitch(cur.x, cur.y, cur.z, cur.yaw, pitch, (1 + angle / 180) * (float) info.settings.gauge.scale(), "RAIL_RIGHT"));
				} else {
					data.add(new VecYawPitch(cur.x, cur.y, cur.z, cur.yaw, pitch, (1 - angle / 180) * (float) info.settings.gauge.scale(), "RAIL_LEFT"));
					data.add(new VecYawPitch(switchPos.x, switchPos.y, switchPos.z, switchPos.yaw, pitch, (1 + angle / 180) * (float) info.settings.gauge.scale(), "RAIL_RIGHT"));
				}
				data.add(new VecYawPitch(cur.x, cur.y, cur.z, cur.yaw, pitch, "RAIL_BASE"));
			} else {
				data.add(new VecYawPitch(cur.x, cur.y, cur.z, cur.yaw, pitch));
			}
		}
		
		return data;
	}
}