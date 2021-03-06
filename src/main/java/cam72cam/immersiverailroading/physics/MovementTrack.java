package cam72cam.immersiverailroading.physics;

import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.track.PosStep;
import cam72cam.immersiverailroading.track.IIterableTrack;
import cam72cam.immersiverailroading.util.VecUtil;
import cam72cam.immersiverailroading.util.math.EnumFacing;
import cam72cam.immersiverailroading.util.math.Vec3d;
import net.minecraft.world.World;
import trackapi.lib.ITrack;
import trackapi.lib.Util;

import java.util.List;

public class MovementTrack {

	public static ITrack findTrack(World world, Vec3d currentPosition, float trainYaw, double gauge) {
		Vec3d[] positions = new Vec3d[] {
				currentPosition,
				currentPosition.add(VecUtil.fromWrongYaw(1, trainYaw)),
				currentPosition.add(VecUtil.fromWrongYaw(-1, trainYaw)),
		};
		
		double[] heightSkew = new double[] {
			0,
			0.25,
			-0.25,
			0.5,
			-0.5,
		};
		
		for (Vec3d pos : positions) {
			for (double height : heightSkew) {
				ITrack te = Util.getTileEntity(world, pos.addVector(0, height + 0.35, 0).getVec(), true);
				if (te != null && Gauge.from(te.getTrackGauge()) == Gauge.from(gauge)) {
					return te;
				}
			}
		}
		return null;
	}
	

	public static Vec3d nextPosition(World world, Vec3d currentPosition, TileRail rail, float trainYaw, double distanceMeters) {
		double maxDelta = 0.5;
		if (distanceMeters > maxDelta) {
			double dist = 0;
			while (dist < distanceMeters - maxDelta) {
				dist += maxDelta;
				ITrack te = findTrack(world, currentPosition, trainYaw, rail.getTrackGauge());
				if (te == null) {
					return currentPosition;
				}
				Vec3d pastPos = currentPosition;
				trackapi.util.Vec3d vec = te.getNextPosition(currentPosition.getVec(), VecUtil.fromWrongYaw(maxDelta, trainYaw).getVec());
				currentPosition = new Vec3d(vec.x,vec.y,vec.z);
				trainYaw = VecUtil.toWrongYaw(pastPos.subtractReverse(currentPosition));
			}

			ITrack te = findTrack(world, currentPosition, trainYaw, rail.getTrackGauge());
			if (te == null) {
				return currentPosition;
			}
			trackapi.util.Vec3d vec =te.getNextPosition(currentPosition.getVec(), VecUtil.fromWrongYaw(distanceMeters % maxDelta, trainYaw).getVec());
			return new Vec3d(vec.x,vec.y,vec.z);
		} else {
			return nextPositionInner(world, currentPosition, rail, trainYaw, distanceMeters);
		}
	}

	public static Vec3d nextPositionInner(World world, Vec3d currentPosition, TileRail rail, float trainYaw, double distanceMeters) {
		Vec3d delta = VecUtil.fromWrongYaw(distanceMeters, trainYaw);
		
		if (rail == null) {
			if (!world.isRemote) {
				return null; // OFF TRACK
			} else {
				return currentPosition.add(delta);
			}
		}

		double distance = delta.lengthVector();
		double heightOffset = 0.35 * rail.info.settings.gauge.scale();

		if (rail.info.settings.type == TrackItems.CROSSING) {
			delta = VecUtil.fromWrongYaw(distance, EnumFacing.fromAngle(trainYaw).getHorizontalAngle());
			return currentPosition.add(delta);
		} else if (rail.info.settings.type == TrackItems.TURNTABLE) {
			double tablePos = rail.getParentTile().info.tablePos;
			
			currentPosition = currentPosition.add(delta);
			
			Vec3d center = new Vec3d(rail.getParentTile().getPos()).addVector(0.5, 1 + heightOffset, 0.5);
			
			double fromCenter = currentPosition.distanceTo(center);
			
			float angle = 360/16.0f * (float)tablePos + rail.info.placementInfo.facing().getHorizontalAngle();
			
			Vec3d forward = center.add(VecUtil.fromWrongYaw(fromCenter, angle));
			Vec3d backward = center.add(VecUtil.fromWrongYaw(fromCenter, angle + 180));
			
			if (forward.distanceTo(currentPosition) < backward.distanceTo(currentPosition)) {
				return forward;
			} else {
				return backward;
			}
		} else if (rail.info.getBuilder() instanceof IIterableTrack) {
			List<PosStep> positions = ((IIterableTrack) rail.info.getBuilder()).getPath(0.5);
			Vec3d center = rail.info.placementInfo.placementPosition;
			Vec3d relative = currentPosition.subtract(center);
			PosStep close = positions.get(0);
			for (PosStep pos : positions) {
				if (close.distanceTo(relative) > pos.distanceTo(relative)) {
					close = pos;
				}
			}
			
			Vec3d estimatedPosition = currentPosition.add(delta);
			
			Vec3d closePos = center.add(close).addVector(0, heightOffset, 0);
			double distToClose = closePos.distanceTo(estimatedPosition);
			
			Vec3d curveDelta = VecUtil.fromYaw(distToClose, close.yaw);

			Vec3d forward = closePos.add(curveDelta);
			Vec3d backward = closePos.subtract(curveDelta);
			
			
			
			if (forward.distanceTo(estimatedPosition) < backward.distanceTo(estimatedPosition)) {
				return forward;
			} else {
				return backward;
			}
		}
		return currentPosition.add(delta);
	}
}
